from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from users.kakao import KakaoService
from users.serializers import UserSerializer, UserGoalSerializer
from rest_framework.permissions import IsAuthenticated


class KakaoLoginView(APIView):
    """
    프론트에서 카카오 Access Token을 보내면:
    1) KakaoService 통해 사용자 정보 조회
    2) CustomUser 생성/업데이트
    3) JWT 토큰 발급
    """

    def post(self, request):
        access_token = request.data.get("access_token", None)

        if not access_token:
            return Response(
                {"error": "access_token 이 필요합니다."},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            result = KakaoService.login_with_kakao(access_token)
            user = result["user"]
            tokens = result["tokens"]

            return Response({
                "user": UserSerializer(user).data,
                "tokens": tokens
            }, status=status.HTTP_200_OK)

        except ValueError as e:
            return Response({"error": str(e)}, status=status.HTTP_400_BAD_REQUEST)
        except Exception as e:
            return Response({"error": f"서버 오류: {str(e)}"},
                            status=status.HTTP_500_INTERNAL_SERVER_ERROR)

class UserGoalUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request):
        user = request.user
        serializer = UserGoalSerializer(user, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response({
                "message": "목표가 성공적으로 수정되었습니다.",
                "goal": serializer.data
            })
        return Response(serializer.errors, status=400)
