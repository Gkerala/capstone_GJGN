import requests
import jwt
from django.conf import settings
from django.contrib.auth.models import User
from rest_framework.response import Response
from rest_framework.decorators import api_view
from rest_framework import status
from rest_framework_simplejwt.tokens import RefreshToken

# 1️⃣ 카카오 로그인 시작
@api_view(['GET'])
def kakao_login(request):
    kakao_auth_url = (
        f"https://kauth.kakao.com/oauth/authorize?"
        f"client_id={settings.KAKAO_REST_API_KEY}&"
        f"redirect_uri={settings.KAKAO_REDIRECT_URI}&"
        f"response_type=code"
    )
    return Response({"auth_url": kakao_auth_url})


# 2️⃣ 카카오 인증 콜백 → JWT 발급
@api_view(['GET'])
def kakao_callback(request):
    code = request.GET.get("code")

    if not code:
        return Response({"error": "인가 코드가 없습니다."}, status=status.HTTP_400_BAD_REQUEST)

    # 1단계: 카카오 액세스 토큰 요청
    token_url = "https://kauth.kakao.com/oauth/token"
    data = {
        "grant_type": "authorization_code",
        "client_id": settings.KAKAO_REST_API_KEY,
        "redirect_uri": settings.KAKAO_REDIRECT_URI,
        "code": code,
    }

    token_response = requests.post(token_url, data=data)
    token_json = token_response.json()

    access_token = token_json.get("access_token")
    if not access_token:
        return Response({"error": "카카오 토큰 발급 실패", "detail": token_json}, status=status.HTTP_400_BAD_REQUEST)

    # 2단계: 사용자 정보 요청
    user_info = requests.get(
        "https://kapi.kakao.com/v2/user/me",
        headers={"Authorization": f"Bearer {access_token}"}
    ).json()

    kakao_id = user_info.get("id")
    kakao_account = user_info.get("kakao_account", {})
    email = kakao_account.get("email", f"kakao_{kakao_id}@example.com")
    nickname = kakao_account.get("profile", {}).get("nickname", "카카오사용자")

    # 3단계: 사용자 생성 or 기존 사용자 가져오기
    user, created = User.objects.get_or_create(username=email, defaults={"first_name": nickname})

    # 4단계: JWT 토큰 발급
    refresh = RefreshToken.for_user(user)
    access = str(refresh.access_token)

    return Response({
        "email": email,
        "nickname": nickname,
        "access_token": access,
        "refresh_token": str(refresh),
        "is_new_user": created,
    })
