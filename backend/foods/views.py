from django.db.models import Q
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.permissions import AllowAny

from .models import Food, FoodAnalysis
from .serializers import FoodSerializer, FoodAnalysisSerializer
from .services.nutrition_service import NutritionService


class FoodListView(APIView):
    def get(self, request):
        foods = Food.objects.all().order_by("-id")
        return Response(FoodSerializer(foods, many=True).data)


class FoodCreateView(APIView):
    parser_classes = [MultiPartParser, FormParser]

    def post(self, request):
        serializer = FoodSerializer(data=request.data)

        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)

        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class FoodAnalyzeView(APIView):
    """
    이미지 업로드 → AI 분석 → FoodAnalysis 생성
    """

    parser_classes = [MultiPartParser, FormParser]

    def post(self, request):
        food_id = request.data.get("food_id")
        image = request.data.get("image")

        if not food_id or not image:
            return Response(
                {"error": "food_id와 image가 필요합니다."},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            food = Food.objects.get(id=food_id)
        except Food.DoesNotExist:
            return Response({"error": "존재하지 않는 음식 ID"}, status=404)

        # ⬇ AI 분석 실행
        analysis = NutritionService.analyze_image(image.temporary_file_path())

        result = FoodAnalysis.objects.create(
            food=food,
            image=image,
            calories=analysis["calories"],
            protein=analysis["protein"],
            carbs=analysis["carbs"],
            fat=analysis["fat"],
            confidence=analysis["confidence"],
        )

        return Response(
            FoodAnalysisSerializer(result).data,
            status=status.HTTP_201_CREATED
        )

class FoodSearchView(APIView):
    """
    GET /api/foods/search/?q=검색어
    음식명 부분 검색
    """
    permission_classes = [AllowAny]

    def get(self, request):
        query = request.query_params.get("q", "").strip()

        if query == "":
            return Response({"results": []}, status=200)

        # 부분 일치 검색
        foods = Food.objects.filter(
            Q(name__icontains=query)
        )[:30]  # 최대 30개 제한

        results = [
            {
                "id": food.id,
                "name": food.name,
                "calories": food.calories,
            }
            for food in foods
        ]

        return Response({"count": len(results), "results": results}, status=200)


class FoodDetailView(APIView):
    """
    GET /api/foods/<id>/
    음식 상세 정보
    """
    permission_classes = [AllowAny]

    def get(self, request, food_id):
        try:
            food = Food.objects.get(id=food_id)
        except Food.DoesNotExist:
            return Response({"error": "Food not found"}, 404)

        data = {
            "id": food.id,
            "name": food.name,
            "calories": food.calories,
            "carbs": food.carbs,
            "protein": food.protein,
            "fat": food.fat,
        }
        return Response(data, 200)