import base64
import requests
from django.conf import settings

class FoodAIService:

    @staticmethod
    def analyze_image(image_file):
        """
        이미지 → 음식 이름 / 양 추정
        실제 AI 연결 부분은 여기만 수정하면 됨.
        """

        # base64 변환
        img_base64 = base64.b64encode(image_file.read()).decode("utf-8")

        # (예시) OpenAI Vision 요청
        url = "https://api.openai.com/v1/chat/completions"
        headers = {"Authorization": f"Bearer {settings.OPENAI_API_KEY}"}

        payload = {
            "model": "gpt-4o-mini",
            "messages": [
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "input_text",
                            "text": "이 음식 사진에서 음식 이름 / 양을 JSON으로 알려줘."
                        },
                        {
                            "type": "input_image",
                            "image_url": f"data:image/jpeg;base64,{img_base64}"
                        }
                    ]
                }
            ],
            "max_output_tokens": 300
        }

        response = requests.post(url, json=payload, headers=headers)
        data = response.json()

        # 예시 응답 포맷
        """
        {
          "foods": [
            {"name": "불고기", "amount": 1.0},
            {"name": "밥", "amount": 0.8}
          ]
        }
        """

        return data["choices"][0]["message"]["content"]
