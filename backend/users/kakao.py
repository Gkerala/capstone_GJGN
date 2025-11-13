# backend/users/kakao.py
import requests
from django.conf import settings

def get_kakao_user_info(code):
    """
    카카오 인가 코드로 access_token을 받아 사용자 정보 반환
    """
    token_url = "https://kauth.kakao.com/oauth/token"
    user_url = "https://kapi.kakao.com/v2/user/me"

    # Access Token 요청
    data = {
        "grant_type": "authorization_code",
        "client_id": settings.KAKAO_REST_API_KEY,
        "redirect_uri": settings.KAKAO_REDIRECT_URI,
        "code": code,
    }
    token_response = requests.post(token_url, data=data)
    token_json = token_response.json()
    access_token = token_json.get("access_token")

    # 사용자 정보 요청
    headers = {"Authorization": f"Bearer {access_token}"}
    user_response = requests.get(user_url, headers=headers)
    user_json = user_response.json()

    kakao_id = user_json.get("id")
    email = user_json.get("kakao_account", {}).get("email", None)
    nickname = user_json.get("kakao_account", {}).get("profile", {}).get("nickname", "")

    return {
        "id": kakao_id,
        "email": email,
        "nickname": nickname,
    }
