import json
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status, permissions

from ai_inference.service_ai import FoodAIService
from foods.models import Food
from .models import MealRecord, MealFood
from .serializers import MealRecordSerializer


class AIFoodRecordCreateView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        if "image" not in request.FILES:
            return Response({"error": "이미지 필요"}, status=400)

        image = request.FILES["image"]

        # 1) AI 분석
        ai_json = FoodAIService.analyze_image(image)
        ai_data = json.loads(ai_json)

        # 2) 식단 기록 생성
        record = MealRecord.objects.create(
            user=request.user,
            meal_time=request.data.get("meal_time"),
            memo=request.data.get("memo", ""),
            image=image,
        )

        # 3) 인식된 음식 → DB에서 영양소 반영
        for f in ai_data["foods"]:
            name = f["name"]
            amount = f["amount"]

            try:
                food_obj = Food.objects.get(name=name)
            except Food.DoesNotExist:
                continue  # 미등록 음식은 제외

            MealFood.objects.create(
                record=record,
                food=food_obj,
                amount=amount,
                calories=food_obj.calories * amount,
                carbs=food_obj.carbs * amount,
                protein=food_obj.protein * amount,
                fat=food_obj.fat * amount,
            )

        return Response(MealRecordSerializer(record).data, status=201)
