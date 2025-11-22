# backend/foods/views_nutrition.py
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from ai_inference.services.nutrition_service import get_nutrition_from_edamam

class NutritionAPIView(APIView):
    def get(self, request):
        name = request.GET.get("name")

        if not name:
            return Response(
                {"success": False, "error": "name parameter missing"},
                status=status.HTTP_400_BAD_REQUEST
            )

        # 🔥 EDAMAM API 호출 (서비스 레이어 재사용)
        nutrition = get_nutrition_from_edamam(name)

        if nutrition is None:
            return Response(
                {"success": False, "error": f"No nutrition data for {name}"},
                status=status.HTTP_404_NOT_FOUND
            )

        return Response({
            "success": True,
            "name": name,
            "kcal": nutrition["kcal"],
            "carb": nutrition["carb"],
            "protein": nutrition["protein"],
            "fat": nutrition["fat"],
            "sugar": nutrition["sugar"],
            "weight": nutrition["weight"],   # EDAMAM에서 받은 weight 그대로 사용
        })
