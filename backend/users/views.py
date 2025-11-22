from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from users.serializers import (
    UserSerializer,
    UserProfileUpdateSerializer,
    FullProfileUpdateSerializer,
)
from users.models import UserGoal


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
        username = request.user.username
        request.user.delete()
        return Response({"message": f"사용자 '{username}' 계정이 삭제되었습니다."}, status=200)
