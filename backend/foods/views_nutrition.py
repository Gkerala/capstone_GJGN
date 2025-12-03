# backend/foods/views_nutrition.py

import requests
from django.conf import settings
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

# YOLO 라벨 → Edamam용 이름 매핑
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

        # 매핑된 이름 사용
        mapped = LABEL_MAPPING.get(raw_name.lower(), raw_name.lower())
        print(f"[NUTRITION API] Edamam 요청용 이름(mapped) = {mapped}")

        # 무료 Nutrition Data API 엔드포인트
        url = "https://api.edamam.com/api/nutrition-data"

        params = {
            "app_id": settings.EDAMAM_APP_ID,
            "app_key": settings.EDAMAM_APP_KEY,
            "ingr": f"100g {mapped}"
        }

        try:
            res = requests.get(url, params=params)
        except Exception as e:
            print("[NUTRITION API] ❌ 요청 실패:", e)
            return self._fail_response(raw_name)

        # JSON 파싱
        try:
            data = res.json()
        except Exception:
            print("[NUTRITION API] ❌ JSON 파싱 오류 → fallback")
            return self._fail_response(raw_name)

        print(f"[NUTRITION API] EDAMAM raw response = {data}")

        # --- 1) 먼저 nutrition-data(기존) 값 확인 ---
        calories = data.get("calories", 0)
        if calories > 0:
            print("[NUTRITION API] 기존 nutrition-data 구조 사용 성공")
            nutrients = data.get("totalNutrients", {})

            result = {
                "success": True,
                "name": raw_name,
                "calories": data.get("calories", 0),
                "carbs": nutrients.get("CHOCDF", {}).get("quantity", 0),
                "protein": nutrients.get("PROCNT", {}).get("quantity", 0),
                "fat": nutrients.get("FAT", {}).get("quantity", 0),
                "sugar": nutrients.get("SUGAR", {}).get("quantity", 0),
                "weight": data.get("totalWeight", 100),
            }


            print(f"[NUTRITION API] 최종 응답 = {result}")
            return Response(result, 200)

        # --- 2) 실패 시 parsed nutrients 구조 사용 ---
        try:
            parsed_item = data["ingredients"][0]["parsed"][0]
            nut = parsed_item["nutrients"]

            result = {
                "success": True,
                "name": raw_name,
                "calories": nut.get("ENERC_KCAL", {}).get("quantity", 0),
                "carbs": nut.get("CHOCDF", {}).get("quantity", 0),
                "protein": nut.get("PROCNT", {}).get("quantity", 0),
                "fat": nut.get("FAT", {}).get("quantity", 0),
                "sugar": nut.get("SUGAR", {}).get("quantity", 0),
                "weight": parsed_item.get("weight", 100),
            }


            print("[NUTRITION API] parsed 구조 사용 성공")
            print(f"[NUTRITION API] 최종 응답 = {result}")
            return Response(result, 200)

        except Exception:
            print("[NUTRITION API] ⚠ parsed 구조 또한 실패 → fallback")
            return self._fail_response(raw_name)

    def _fail_response(self, raw_name):
        """
        분석 실패 시 기존과 동일한 zero-response 반환
        """
        print("[NUTRITION API] ⚠ 영양 분석 실패 → zero 반환")
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
