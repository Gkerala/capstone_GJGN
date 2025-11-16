from rest_framework.views import APIView
from rest_framework.response import Response

DUMMY_NUTRITION = {
    "apple":      {"grams": 100, "calories": 52,  "carbs": 14,  "protein": 0.3, "fat": 0.2, "sugar": 10},
    "chapathi":   {"grams": 100, "calories": 120, "carbs": 18,  "protein": 3,   "fat": 4,   "sugar": 1},
    "chicken Gravy": {"grams": 100, "calories": 240, "carbs": 8, "protein": 20, "fat": 15, "sugar": 3},
    "fries":      {"grams": 100, "calories": 312, "carbs": 41,  "protein": 3.4, "fat": 15, "sugar": 0.3},
    "idli":       {"grams": 100, "calories": 55,  "carbs": 12,  "protein": 2,   "fat": 0.4, "sugar": 0},
    "pizza":      {"grams": 100, "calories": 266, "carbs": 33,  "protein": 11,  "fat": 10, "sugar": 3.6},
    "rice":       {"grams": 100, "calories": 130, "carbs": 28,  "protein": 2.7, "fat": 0.3, "sugar": 0},
    "soda":       {"grams": 100, "calories": 150, "carbs": 39,  "protein": 0,   "fat": 0,   "sugar": 39},
    "tomato":     {"grams": 100, "calories": 18,  "carbs": 3.9, "protein": 0.9, "fat": 0.2, "sugar": 2.6},
    "vada":       {"grams": 100, "calories": 97,  "carbs": 8,   "protein": 2,   "fat": 6,   "sugar": 0},
    "banana":     {"grams": 100, "calories": 89,  "carbs": 23,  "protein": 1.1, "fat": 0.3, "sugar": 12},
    "burger":     {"grams": 100, "calories": 295, "carbs": 30,  "protein": 17,  "fat": 13, "sugar": 6}
}


class NutritionAPIView(APIView):
    def get(self, request):
        food = request.GET.get("name")

        if not food:
            return Response({"success": False, "error": "no name provided"}, status=400)

        # 🔥 숨겨진 공백 / 줄바꿈 제거
        food = food.strip()

        # 🔥 DUMMY 전체를 lowercase로 변환
        normalized_db = {k.lower().strip(): v for k, v in DUMMY_NUTRITION.items()}
        key = food.lower().strip()

        print(f"[DEBUG] received name = '{food}', lookup key = '{key}'")

        data = normalized_db.get(key)

        if not data:
            return Response({
                "success": False,
                "error": f"No nutrition data found for {food}"
            })

        return Response({
            "success": True,
            "name": food,
            "grams": data["grams"],         
            "calories": data["calories"],
            "carbs": data["carbs"],
            "protein": data["protein"],
            "fat": data["fat"],
            "sugar": data["sugar"],
            "serving_size": f"{data['grams']}g"
        })

