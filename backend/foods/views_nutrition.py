from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from .models import Food
from django.db.models import Q

class NutritionAPIView(APIView):
    def get(self, request):
        name = request.GET.get("name")

        if not name:
            return Response(
                {"success": False, "error": "name parameter missing"},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # 입력 정리
        query = name.strip().lower()

        # 1) 완전 일치 검색
        food = Food.objects.filter(name__iexact=query).first()

        # 2) 영문/한글 포함 검색
        if not food:
            food = Food.objects.filter(name__icontains=query).first()

        # 3) 대문자/소문자 변형
        if not food:
            food = Food.objects.filter(name__icontains=name).first()

        if not food:
            return Response(
                {"success": False, "error": f"No nutrition data for '{name}'"},
                status=status.HTTP_404_NOT_FOUND
            )

        # 응답 구조 → 앱 NutritionResponse와 정확히 동일하게
        return Response({
            "success": True,
            "name": food.name,
            "kcal": float(food.calories),
            "carb": float(food.carbs),
            "protein": float(food.protein),
            "fat": float(food.fat),
            "sugar": 0,                # Food 모델에는 sugar 없음 → default
            "weight": 100.0            # 기본 100g
        })
