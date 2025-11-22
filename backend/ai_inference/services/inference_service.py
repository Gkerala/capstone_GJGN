# backend/ai_inference/services/inference_service.py
from .nutrition_service import get_nutrition_from_edamam

def process_food_results(yolo_results):
    processed = []

    for item in yolo_results:
        food_name = item["name"]

        nutrition = get_nutrition_from_edamam(food_name)

        if nutrition is None:
            continue

        processed.append({
            "name": food_name,
            "kcal": nutrition["kcal"],
            "weight": nutrition["weight"],
            "carb": nutrition["carb"],
            "protein": nutrition["protein"],
            "fat": nutrition["fat"],
            "sugar": nutrition["sugar"],
        })

    return processed
