# backend/ai_inference/services/nutrition_service.py
import requests
from django.conf import settings

def get_nutrition_from_edamam(food_name: str) -> dict:
    url = "https://api.edamam.com/api/nutrition-data"
    
    params = {
        "app_id": settings.EDAMAM_APP_ID,
        "app_key": settings.EDAMAM_APP_KEY,
        "ingr": food_name
    }

    response = requests.get(url, params=params)

    if response.status_code != 200:
        return None

    data = response.json()

    nutrients = data.get("totalNutrients", {})

    return {
        "kcal": data.get("calories", 0),
        "weight": data.get("totalWeight", 0),

        # 탄·단·지·당
        "carb": nutrients.get("CHOCDF", {}).get("quantity", 0),
        "protein": nutrients.get("PROCNT", {}).get("quantity", 0),
        "fat": nutrients.get("FAT", {}).get("quantity", 0),
        "sugar": nutrients.get("SUGAR", {}).get("quantity", 0),
    }
