from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.contrib.auth.models import User
from rest_framework.permissions import IsAuthenticated
from rest_framework_simplejwt.tokens import RefreshToken
from .kakao import get_kakao_user_info


class KakaoLoginView(APIView):
    """
    프론트엔드에서 access_token 받아
    → 카카오 API 호출
    → 유저 등록 or 로그인
    → JWT 발급 후 반환
    """

    def post(self, request):
        try:
            access_token = request.data.get("access_token")
            if not access_token:
                return Response(
                    {"error": "access_token이 필요합니다."},
                    status=status.HTTP_400_BAD_REQUEST,
                )

            # ✅ 카카오 사용자 정보 요청
            kakao_user = get_kakao_user_info(access_token)
            kakao_id = kakao_user.get("id")

            if not kakao_id:
                return Response(
                    {"error": "카카오 사용자 정보 조회 실패"},
                    status=status.HTTP_400_BAD_REQUEST,
                )

            email = kakao_user.get("email") or f"kakao_{kakao_id}@kakao.com"
            nickname = kakao_user.get("nickname") or "KakaoUser"

            # ✅ 유저 생성 또는 로그인 처리
            user, created = User.objects.get_or_create(
                username=f"kakao_{kakao_id}",
                defaults={"email": email, "first_name": nickname},
            )

            # ✅ JWT 토큰 발급
            refresh = RefreshToken.for_user(user)

            return Response(
                {
                    "message": "회원가입 완료" if created else "로그인 성공",
                    "user": {
                        "username": user.username,
                        "email": user.email,
                        "nickname": user.first_name,
                    },
                    "access_token": str(refresh.access_token),
                    "refresh_token": str(refresh),
                },
                status=status.HTTP_200_OK,
            )

        except Exception as e:
            return Response(
                {"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )


class JWTTestView(APIView):
    """
    JWT 인증이 잘 작동하는지 테스트용 API
    """

    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        return Response(
            {"message": "JWT 인증 성공", "user": user.username},
            status=status.HTTP_200_OK,
        )
