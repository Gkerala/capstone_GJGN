import requests
from django.conf import settings
from foods.models import Food
from records.models import Record

class AIRecordService:

    @staticmethod
    def analyze_image_and_create_records(user, image):
        """
        1) AI 서버에 이미지 전송
        2) 음식 후보 리스트 반환 (ex: ["김치찌개", "밥", "달걀말이"])
        3) DB 매칭 후 자동 기록 생성
        """
        ai_url = settings.AI_INFERENCE_URL

        # 1) 이미지 업로드 → AI 서버 예측 요청
        files = {"image": image}
        try:
            response = requests.post(ai_url, files=files, timeout=10)
            response.raise_for_status()
        except Exception as e:
            return {"error": f"AI 서버 연결 실패: {e}"}

        data = response.json()

        # 예시: { "foods": ["계란후라이", "밥"] }
        predicted_foods = data.get("foods", [])
        if not predicted_foods:
            return {"error": "음식 인식 실패 (no foods returned)"}

        # 2) DB에서 음식 영양 정보 매칭
        found_foods = Food.objects.filter(name__in=predicted_foods)

        if not found_foods.exists():
            return {"error": "DB에 일치하는 음식이 없습니다."}

        created_records = []
        total_cal = 0
        total_carbs = 0
        total_protein = 0
        total_fat = 0

        # 3) 자동 Record 생성
        for food in found_foods:
            record = Record.objects.create(
                user=user,
                food=food,
                amount=1  # 기본값 1회 제공량
            )
            created_records.append(record.id)

            total_cal += food.calories
            total_carbs += food.carbs
            total_protein += food.protein
            total_fat += food.fat

        # 4) 반환
        return {
            "predicted": predicted_foods,
            "created_records": created_records,
            "total_calories": total_cal,
            "total_carbs": total_carbs,
            "total_protein": total_protein,
            "total_fat": total_fat,
        }
