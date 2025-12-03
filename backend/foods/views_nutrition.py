# backend/foods/views_nutrition.py

import requests
from django.conf import settings
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from foods.models import NutritionCache


# ------------------------------------------------------
# YOLO 라벨 → Edamam 검색용 이름 매핑
# ------------------------------------------------------
LABEL_MAPPING = {
    "rice": "cooked white rice",
    "sushi": "salmon sushi roll",
    "tempura bowl": "tempura rice bowl (tendon)",
    "udon noodle": "cooked udon noodles",
    "tempura udon": "udon with tempura",
    "soba noodle": "cooked soba noodles",
    "ramen noodle": "Japanese ramen (noodles with broth)",
    "Japanese-style pancake": "okonomiyaki (Japanese savory pancake)",
    "takoyaki": "takoyaki (octopus balls)",
    "vegetable tempura": "vegetable tempura (fried vegetables)",
    "miso soup": "miso soup",
    "grilled salmon": "grilled salmon",
    "rice ball": "onigiri rice ball",
    "dry curry": "Japanese dry curry with rice",
    "spicy chili-flavored tofu": "mapo tofu (spicy tofu dish)",
    "fried chicken": "Japanese fried chicken (karaage)",
    "fried fish": "fried white fish (battered)",
    "pork cutlet on rice": "katsudon (pork cutlet rice bowl)",
    "beef curry": "Japanese beef curry with rice",
    "broiled eel bowl": "unagi don (broiled eel rice bowl)"
}


# ------------------------------------------------------
# 통합 파싱 함수 (기존 + 새로운 구조 모두 지원)
# ------------------------------------------------------
def parse_edamam_response(data):
    """
    Edamam 응답을 기존 totalNutrients 구조 + 새로운 parsed 구조 모두 처리
    """

    # ------------------------------
    # 1) 기존 구조: calories + totalNutrients
    # ------------------------------
    if "calories" in data and "totalNutrients" in data:
        nutrients = data.get("totalNutrients", {})
        return {
            "calories": data.get("calories", 0),
            "carbs": nutrients.get("CHOCDF", {}).get("quantity", 0),
            "protein": nutrients.get("PROCNT", {}).get("quantity", 0),
            "fat": nutrients.get("FAT", {}).get("quantity", 0),
            "sugar": nutrients.get("SUGAR", {}).get("quantity", 0),
            "weight": data.get("totalWeight", 100),
        }

    # ------------------------------
    # 2) 새로운 구조: ingredients → parsed → nutrients
    # ------------------------------
    try:
        ingredients = data.get("ingredients", [])
        if ingredients:
            parsed_list = ingredients[0].get("parsed", [])
            if parsed_list:
                p = parsed_list[0]
                nutrients = p.get("nutrients", {})
                weight = p.get("weight", 100)
                return {
                    "calories": nutrients.get("ENERC_KCAL", {}).get("quantity", 0),
                    "carbs": nutrients.get("CHOCDF", {}).get("quantity", 0),
                    "protein": nutrients.get("PROCNT", {}).get("quantity", 0),
                    "fat": nutrients.get("FAT", {}).get("quantity", 0),
                    "sugar": nutrients.get("SUGAR", {}).get("quantity", 0),
                    "weight": weight,
                }
    except Exception as e:
        print(f"[NUTRITION API] New parser exception: {e}")

    # ------------------------------
    # 3) 모든 구조 매칭 실패 → None
    # ------------------------------
    return None


# ------------------------------------------------------
# Nutrition API View
# ------------------------------------------------------
class NutritionAPIView(APIView):

    def get(self, request):
        raw_name = request.GET.get("name")

        print("=" * 60)
        print(f"[NUTRITION API] 요청 이름(raw) = {raw_name}")

        if not raw_name:
            return Response(
                {"success": False, "error": "name parameter missing"},
                status=status.HTTP_400_BAD_REQUEST
            )

        mapped = LABEL_MAPPING.get(raw_name.lower(), raw_name.lower())
        print(f"[NUTRITION API] Edamam 요청용 이름(mapped) = {mapped}")

        # ------------------------------------------------------
        # 1) DB 캐싱 조회
        # ------------------------------------------------------
        try:
            cached = NutritionCache.objects.get(name=raw_name.lower())
            print("[NUTRITION API] 🔵 DB 캐싱된 결과 사용!")

            return Response({
                "success": True,
                "name": raw_name,
                "calories": cached.calories,
                "carbs": cached.carbs,
                "protein": cached.protein,
                "fat": cached.fat,
                "sugar": cached.sugar,
                "weight": 100,
            }, 200)

        except NutritionCache.DoesNotExist:
            print("[NUTRITION API] 🟡 DB에 없음 → Edamam API 호출")

        # ------------------------------------------------------
        # 2) Edamam API 호출
        # ------------------------------------------------------
        url = "https://api.edamam.com/api/nutrition-data"
        params = {
            "app_id": settings.EDAMAM_APP_ID,
            "app_key": settings.EDAMAM_APP_KEY,
            "ingr": f"100g {mapped}"
        }

        try:
            res = requests.get(url, params=params)
            data = res.json()
        except Exception as e:
            print("[NUTRITION API] ❌ 요청/파싱 실패:", e)
            return self._zero_response(raw_name)

        print("[NUTRITION API] Edamam raw response:", data)

        # ------------------------------------------------------
        # 3) 통합 파서 적용
        # ------------------------------------------------------
        parsed = parse_edamam_response(data)

        if parsed:
            # DB 저장
            NutritionCache.objects.create(
                name=raw_name.lower(),
                calories=parsed["calories"],
                carbs=parsed["carbs"],
                protein=parsed["protein"],
                fat=parsed["fat"],
                sugar=parsed["sugar"],
            )

            print("[NUTRITION API] 🟢 파싱 성공 → DB 저장 후 반환")
            return Response({
                "success": True,
                "name": raw_name,
                **parsed
            }, 200)

        # ------------------------------------------------------
        # 4) 파싱 실패 → 기존 zero 응답 유지
        # ------------------------------------------------------
        print("[NUTRITION API] ⚠ 파싱 실패 → zero fallback")
        return self._zero_response(raw_name)

    # ------------------------------------------------------
    # 실패 시 기존 앱 응답 유지 (zero)
    # ------------------------------------------------------
    def _zero_response(self, raw_name):
        return Response({
            "success": True,
            "name": raw_name,
            "calories": 0,
            "carbs": 0,
            "protein": 0,
            "fat": 0,
            "sugar": 0,
            "weight": 100,
        }, 200)
