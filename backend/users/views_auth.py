from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from users.kakao import KakaoService
from users.serializers import UserSerializer
from rest_framework.permissions import IsAuthenticated


class KakaoLoginView(APIView):
    def post(self, request):
        access_token = request.data.get("access_token")

        if not access_token:
            return Response({"error": "access_token 이 필요합니다."}, status=400)

        try:
            result = KakaoService.login_with_kakao(access_token)
            user = result["user"]
            tokens = result["tokens"]

            return Response({
                "user": UserSerializer(user).data,
                "access": tokens.get("access"),
                "refresh": tokens.get("refresh"),
                "is_new_user": result["is_new_user"] or not result["is_profile_complete"],
                "is_profile_complete": result["is_profile_complete"]
            })

        except Exception as e:
            return Response({"error": str(e)}, status=500)
