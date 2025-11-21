#backend/users/views_auth.py
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from users.kakao import KakaoService
from users.serializers import UserSerializer, UserGoalUpdateSerializer, UserProfileUpdateSerializer
from rest_framework.permissions import IsAuthenticated
from users.models import UserGoal


class KakaoLoginView(APIView):
    def post(self, request):
        access_token = request.data.get("access_token")

        if not access_token:
            return Response(
                {"error": "access_token 이 필요합니다."},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            result = KakaoService.login_with_kakao(access_token)
            user = result["user"]
            tokens = result["tokens"]

            response_data = {
                "user": UserSerializer(user).data,
                "access": tokens.get("access"),
                "refresh": tokens.get("refresh"),
                "is_new_user": result["is_new_user"] or not result["is_profile_complete"],
                "is_profile_complete": result["is_profile_complete"]
            }

            print("\n=== 📌 Kakao Login Response JSON ===")
            print(response_data)
            print("====================================\n")

            return Response(response_data, status=status.HTTP_200_OK)

        except ValueError as e:
            return Response({"error": str(e)}, status=status.HTTP_400_BAD_REQUEST)
        except Exception as e:
            return Response({"error": f"서버 오류: {str(e)}"},
                            status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class UserGoalUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request):
        user = request.user
        goal, _ = UserGoal.objects.get_or_create(user=user)

        serializer = UserGoalUpdateSerializer(goal, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response({
                "message": "목표가 성공적으로 수정되었습니다.",
                "goal": serializer.data
            })
        return Response(serializer.errors, status=400)

class UserMeView(APIView):
    permission_classes = [IsAuthenticated]

    # GET /api/users/me/
    def get(self, request):
        serializer = UserSerializer(request.user)
        return Response(serializer.data, status=status.HTTP_200_OK)

    # PATCH /api/users/me/
    def patch(self, request):
        serializer = UserProfileUpdateSerializer(
            request.user, data=request.data, partial=True
        )
        if serializer.is_valid():
            serializer.save()
            return Response({
                "message": "유저 정보가 업데이트되었습니다.",
                "user": serializer.data
            })
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)