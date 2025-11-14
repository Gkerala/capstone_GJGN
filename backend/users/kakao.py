import os
import requests
from dotenv import load_dotenv
from django.contrib.auth import get_user_model
from users.services.jwt_service import generate_jwt_tokens

load_dotenv()  # .env 파일 로딩

User = get_user_model()

KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"

KAKAO_REST_API_KEY = os.getenv("KAKAO_REST_API_KEY")
KAKAO_REDIRECT_URI = os.getenv("KAKAO_REDIRECT_URI")


class KakaoService:
    """
    Kakao Access Token → Kakao User Info → CustomUser 생성/조회 → JWT 발급
    """

    @staticmethod
    def get_kakao_user_info(access_token: str) -> dict:
        """카카오 access token으로 사용자 정보 요청"""
        headers = {
            "Authorization": f"Bearer {access_token}",
            "Content-Type": "application/x-www-form-urlencoded;charset=utf-8",
        }

        response = requests.get(KAKAO_USER_INFO_URL, headers=headers)

        if response.status_code != 200:
            raise ValueError("카카오 사용자 정보 요청 실패")

        return response.json()

    @staticmethod
    def get_or_create_user(kakao_data: dict) -> User:
        """카카오 데이터 기반 CustomUser 조회/생성"""

        kakao_id = kakao_data.get("id")
        if not kakao_id:
            raise ValueError("카카오 ID 없음")

        kakao_account = kakao_data.get("kakao_account", {})
        profile = kakao_account.get("profile", {})

        nickname = profile.get("nickname", "카카오사용자")
        profile_image = profile.get("profile_image_url", "")

        email = kakao_account.get("email")  # optional

        # CustomUser 에 kakao_id 필드가 반드시 있어야 한다!!
        user, created = User.objects.get_or_create(
            kakao_id=kakao_id,
            defaults={
                "username": f"kakao_{kakao_id}",
                "nickname": nickname,
                "email": email,
                "profile_image": profile_image,
            },
        )

        # nickname이나 프로필이 바뀌면 업데이트
        if not created:
            update_needed = False

            if nickname and user.nickname != nickname:
                user.nickname = nickname
                update_needed = True

            if profile_image and user.profile_image != profile_image:
                user.profile_image = profile_image
                update_needed = True

            if email and user.email != email:
                user.email = email
                update_needed = True

            if update_needed:
                user.save()

        return user

    @staticmethod
    def login_with_kakao(access_token: str) -> dict:
        """카카오 로그인 → User 반환 + JWT 생성"""
        kakao_data = KakaoService.get_kakao_user_info(access_token)
        user = KakaoService.get_or_create_user(kakao_data)

        # JWT 생성
        tokens = generate_jwt_tokens(user)

        return {
            "user": user,
            "tokens": tokens,
        }
