# backend/users/views.py
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from django.contrib.auth.models import User
from .kakao import get_kakao_user_info

class KakaoLoginView(APIView):
    def get(self, request):
        code = request.GET.get("code")
        user_info = get_kakao_user_info(code)

        # 기존 유저 확인 또는 생성
        user, created = User.objects.get_or_create(
            username=user_info["id"],
            defaults={"email": user_info["email"] or "", "first_name": user_info["nickname"]},
        )

        # JWT 발급
        refresh = RefreshToken.for_user(user)
        return Response({
            "refresh": str(refresh),
            "access": str(refresh.access_token),
            "nickname": user.first_name,
        })
