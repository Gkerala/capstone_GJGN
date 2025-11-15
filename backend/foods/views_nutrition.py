from rest_framework.views import APIView
from rest_framework.response import Response

DUMMY_NUTRITION = {
    "Apple":      {"calories": 52,  "carbs": 14, "protein": 0.3, "fat": 0.2, "sugar": 10},
    "Chapathi":   {"calories": 120, "carbs": 18, "protein": 3,   "fat": 4,   "sugar": 1},
    "Chicken Gravy": {"calories": 240, "carbs": 8, "protein": 20, "fat": 15, "sugar": 3},
    "Fries":      {"calories": 312, "carbs": 41, "protein": 3.4, "fat": 15,  "sugar": 0.3},
    "Idli":       {"calories": 55,  "carbs": 12, "protein": 2,   "fat": 0.4, "sugar": 0},
    "Pizza":      {"calories": 266, "carbs": 33, "protein": 11,  "fat": 10,  "sugar": 3.6},
    "Rice":       {"calories": 130, "carbs": 28, "protein": 2.7, "fat": 0.3, "sugar": 0},
    "Soda":       {"calories": 150, "carbs": 39, "protein": 0,   "fat": 0,   "sugar": 39},
    "Tomato":     {"calories": 18,  "carbs": 3.9, "protein": 0.9, "fat": 0.2, "sugar": 2.6},
    "Vada":       {"calories": 97,  "carbs": 8,   "protein": 2,   "fat": 6,   "sugar": 0},
    "banana":     {"calories": 89,  "carbs": 23,  "protein": 1.1, "fat": 0.3, "sugar": 12},
    "burger":     {"calories": 295, "carbs": 30,  "protein": 17,  "fat": 13,  "sugar": 6}
}

class NutritionAPIView(APIView):
    def get(self, request):
        food = request.GET.get("name")

        if not food:
            return Response({"success": False, "error": "no name provided"}, status=400)

        data = DUMMY_NUTRITION.get(food)

        if not data:
            return Response({"success": False, "error": "No nutrition data found"})

        return Response({
            "success": True,
            "name": food,
            "calories": data["calories"],
            "carbs": data["carbs"],
            "protein": data["protein"],
            "fat": data["fat"],
            "sugar": data["sugar"],
            "serving_size": "100g"
        })
