from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from users.serializers import (
    UserSerializer,
    UserProfileUpdateSerializer,
    FullProfileUpdateSerializer,
)


class UserDetailView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        serializer = UserSerializer(request.user)
        return Response(serializer.data, status=200)

    def patch(self, request):
        serializer = UserProfileUpdateSerializer(
            request.user, data=request.data, partial=True
        )
        if serializer.is_valid():
            serializer.save()
            return Response({"message": "사용자 정보 업데이트 완료", "user": serializer.data})
        return Response(serializer.errors, status=400)


class UserFullProfileUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def put(self, request):
        serializer = FullProfileUpdateSerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.update(request.user, serializer.validated_data)
            return Response({"message": "프로필 저장 완료", "user": UserSerializer(user).data})
        return Response(serializer.errors, status=400)



class UserDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        user = request.user
        username = user.username

        # ----------------------------------------------------
        # 1) Refresh Token 가져오기 (쿠키 또는 body)
        # ----------------------------------------------------
        refresh_token = (
            request.COOKIES.get("refresh") or
            request.data.get("refresh") or
            request.headers.get("X-Refresh-Token")
        )

        # ----------------------------------------------------
        # 2) Refresh Token 블랙리스트 처리
        # ----------------------------------------------------
        if refresh_token:
            try:
                token = RefreshToken(refresh_token)
                token.blacklist()
            except Exception:
                pass

        # ----------------------------------------------------
        # 3) Access Token 무효화 (SimpleJWT는 Access는 블랙리스트 불가 → 앱에서 삭제 방식)
        #    → 서버에서는 실제로 Access Token을 “사용 불가”로 만드는 방식 없음.
        #    → 대신 유저 자체를 삭제하므로 AccessToken은 인증 단계에서 무조건 실패됨.
        # ----------------------------------------------------

        # ----------------------------------------------------
        # 4) 유저 및 연관 DB 삭제
        # ----------------------------------------------------
        user.delete()

        # ----------------------------------------------------
        # 5) 쿠키 삭제 (웹 환경 대비)
        # ----------------------------------------------------
        response = Response(
            {"message": f"사용자 '{username}' 계정이 삭제되었습니다.", "status": "success"},
            status=200
        )
        response.delete_cookie("access")
        response.delete_cookie("refresh")

        return response
