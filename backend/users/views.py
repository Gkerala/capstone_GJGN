# backend/users/views.py
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from users.serializers import UserSerializer, UserProfileUpdateSerializer, FullProfileUpdateSerializer


class UserDetailView(APIView):
    permission_classes = [IsAuthenticated]

    # GET /api/users/me/
    def get(self, request):
        serializer = UserSerializer(request.user)
        return Response(serializer.data, status=status.HTTP_200_OK)

    # PATCH /api/users/me/
    def patch(self, request):
        return self.update_profile(request)

    # PUT /api/users/me/
    def put(self, request):
        return self.update_profile(request)

    # 실제 업데이트 처리 함수
    def update_profile(self, request):
        serializer = UserProfileUpdateSerializer(
            request.user, data=request.data, partial=True
        )

        if serializer.is_valid():
            serializer.save()
            return Response({
                "message": "사용자 정보가 업데이트되었습니다.",
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
                "message": "프로필이 성공적으로 저장되었습니다.",
                "user": UserSerializer(user).data
            })

        return Response(serializer.errors, status=400)