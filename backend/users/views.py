# backend/users/views.py
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from users.serializers import (
    UserSerializer,
    UserProfileUpdateSerializer,
    FullProfileUpdateSerializer
)


class UserDetailView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        serializer = UserSerializer(request.user)
        return Response(serializer.data, status=status.HTTP_200_OK)

    def patch(self, request):
        return self.update_profile(request)

    def put(self, request):
        return self.update_profile(request)

    def update_profile(self, request):
        serializer = UserProfileUpdateSerializer(
            request.user, data=request.data, partial=True
        )

        if serializer.is_valid():
            serializer.save()
            return Response({
                "message": "사용자 정보 업데이트 완료",
                "user": serializer.data
            }, status=status.HTTP_200_OK)

        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class UserFullProfileUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def put(self, request):
        serializer = FullProfileUpdateSerializer(data=request.data)

        if serializer.is_valid():
            user = serializer.update(request.user, serializer.validated_data)
            return Response({
                "message": "프로필 저장 완료",
                "user": UserSerializer(user).data
            }, status=200)

        return Response(serializer.errors, status=400)


# ⭐ 추가된 회원탈퇴 기능
class UserDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        user = request.user
        username = user.username

        user.delete()

        return Response(
            {"message": f"사용자 '{username}' 계정이 삭제되었습니다."},
            status=status.HTTP_200_OK
        )
