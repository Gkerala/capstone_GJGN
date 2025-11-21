#backend/users/kakao.py
import os
import requests
from dotenv import load_dotenv
from django.contrib.auth import get_user_model
from users.services.jwt_service import JWTService

load_dotenv()

User = get_user_model()

KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"

KAKAO_REST_API_KEY = os.getenv("KAKAO_REST_API_KEY")
KAKAO_REDIRECT_URI = os.getenv("KAKAO_REDIRECT_URI")


class KakaoService:
    """
    Kakao Access Token → Kakao User Info → CustomUser 조회/생성 → JWT 발급
    """

    @staticmethod
    def get_kakao_user_info(access_token: str) -> dict:
        headers = {
            "Authorization": f"Bearer {access_token}",
            "Content-Type": "application/x-www-form-urlencoded;charset=utf-8",
        }

        response = requests.get(KAKAO_USER_INFO_URL, headers=headers)

        if response.status_code != 200:
            raise ValueError("카카오 사용자 정보 요청 실패")

        return response.json()

    @staticmethod
    def get_or_create_user(kakao_data: dict):
        kakao_id = kakao_data.get("id")
        if not kakao_id:
            raise ValueError("카카오 ID 없음")

        kakao_account = kakao_data.get("kakao_account", {})
        profile = kakao_account.get("profile", {})

        nickname = profile.get("nickname", "카카오사용자")
        profile_image = profile.get("profile_image_url", "")
        email = kakao_account.get("email")

        user, created = User.objects.get_or_create(
            kakao_id=kakao_id,
            defaults={
                "username": f"kakao_{kakao_id}",
                "nickname": nickname,
                "email": email,
                "profile_image": profile_image,
            },
        )

        # 기존 사용자 프로필 업데이트
        if not created:
            updated = False

            if nickname and user.nickname != nickname:
                user.nickname = nickname
                updated = True

            if profile_image and user.profile_image != profile_image:
                user.profile_image = profile_image
                updated = True

            if email and user.email != email:
                user.email = email
                updated = True

            if updated:
                user.save()

        return user, created

    @staticmethod
    def login_with_kakao(access_token: str) -> dict:
        kakao_data = KakaoService.get_kakao_user_info(access_token)

        user, created = KakaoService.get_or_create_user(kakao_data)

        # 🔥 프로필이 완성되었는지 체크
        profile_fields = [
            user.height,
            user.weight,
            user.age,
            user.gender,
            user.activity_level
        ]

        is_profile_complete = all(profile_fields)

        tokens = JWTService.generate_tokens(user)

        return {
            "user": user,
            "tokens": tokens,
            "is_new_user": created,
            "is_profile_complete": is_profile_complete,
        }
