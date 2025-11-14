import random

class NutritionService:
    """
    음식 이미지를 기반으로 영양소 분석
    실제 AI 모델 연동 가능하도록 설계
    """

    @staticmethod
    def analyze_image(image_path: str):
        """
        image_path → AI inference 로직 처리
        현재는 Mock 값 반환 (실제 모델 연결 가능)
        """

        # 추후: YOLO + Naver OCR + custom nutrition model 연결
        calories = round(random.uniform(100, 800), 1)
        protein = round(random.uniform(5, 40), 1)
        carbs = round(random.uniform(10, 120), 1)
        fat = round(random.uniform(3, 30), 1)

        return {
            "calories": calories,
            "protein": protein,
            "carbs": carbs,
            "fat": fat,
            "confidence": round(random.uniform(0.7, 0.99), 2),
        }
