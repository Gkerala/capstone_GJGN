# views.py
import requests
from django.contrib.auth.models import User
from django.db.models import Sum
from rest_framework import viewsets, status, permissions
from rest_framework.decorators import api_view, permission_classes, action
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
# from rest_framework_simplejwt.authentication import JWTAuthentication  # (not used directly here)

from .models import UserInfo, FoodNutrition, DietRecord
from .serializers import UserInfoSerializer, FoodNutritionSerializer, DietRecordSerializer


# [1] 카카오 로그인 → JWT 발급
@api_view(['POST'])
@permission_classes([AllowAny])
def kakao_login(request):
    access_token = request.data.get("access_token")
    if not access_token:
        return Response({"error": "access_token is required"}, status=status.HTTP_400_BAD_REQUEST)

    kakao_response = requests.get(
        "https://kapi.kakao.com/v2/user/me",
        headers={"Authorization": f"Bearer {access_token}"}
    )

    if kakao_response.status_code != 200:
        return Response({"error": "Invalid Kakao token"}, status=status.HTTP_400_BAD_REQUEST)

    kakao_data = kakao_response.json()
    kakao_id = kakao_data.get("id")
    kakao_account = kakao_data.get("kakao_account", {})
    email = kakao_account.get("email", f"{kakao_id}@kakao.com")
    nickname = kakao_account.get("profile", {}).get("nickname", "사용자")

    user, created_user = User.objects.get_or_create(
        username=email,
        defaults={"email": email, "first_name": nickname}
    )

    user_info, created_info = UserInfo.objects.get_or_create(
        user=user,
        defaults={
            "username": nickname,
            "email": email,
            "gender": "M",
            "height_cm": 0,
            "weight_kg": 0,
            "goal_type": "maintain",
            "target_weight_kg": 0,
            "activity_level": "moderate"
        }
    )

    refresh = RefreshToken.for_user(user)
    serializer = UserInfoSerializer(user_info)

    return Response({
        "access": str(refresh.access_token),
        "refresh": str(refresh),
        "user": serializer.data
    }, status=status.HTTP_200_OK)


# [2] 사용자 정보 CRUD (회원등록/조회용)
class UserInfoViewSet(viewsets.ModelViewSet):
    """
    POST /api/users/ : 회원정보 등록 (or update if exists)
    GET  /api/users/ : 전체 사용자
    GET  /api/users/<id>/ : 특정 사용자
    """
    queryset = UserInfo.objects.all()
    serializer_class = UserInfoSerializer
    permission_classes = [permissions.IsAuthenticated]

    def create(self, request, *args, **kwargs):
        email = request.data.get('email')
        user = User.objects.filter(email=email).first()

        if not user:
            return Response({"detail": "등록되지 않은 사용자입니다."}, status=status.HTTP_400_BAD_REQUEST)

        existing_info = UserInfo.objects.filter(user=user).first()

        if existing_info:
            serializer = self.get_serializer(existing_info, data=request.data, partial=True)
            serializer.is_valid(raise_exception=True)
            serializer.save(user=user)
            return Response(serializer.data, status=status.HTTP_200_OK)

        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        serializer.save(user=user)
        return Response(serializer.data, status=status.HTTP_201_CREATED)


# [3] 로그인한 사용자 본인 프로필 조회 (클래스형 뷰)
class CurrentUserView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        try:
            user_info = UserInfo.objects.get(user=request.user)
        except UserInfo.DoesNotExist:
            return Response({"detail": "UserInfo not found."}, status=status.HTTP_404_NOT_FOUND)

        serializer = UserInfoSerializer(user_info)
        return Response(serializer.data, status=status.HTTP_200_OK)


# [4] 음식 영양소 관리
class FoodNutritionViewSet(viewsets.ModelViewSet):
    queryset = FoodNutrition.objects.all()
    serializer_class = FoodNutritionSerializer
    permission_classes = [IsAuthenticated]


# [5] 식단 기록 관리
class DietRecordViewSet(viewsets.ModelViewSet):
    queryset = DietRecord.objects.select_related('user', 'food').all()
    serializer_class = DietRecordSerializer
    permission_classes = [IsAuthenticated]

    def perform_create(self, serializer):
        serializer.save()

    @action(detail=False, methods=['get'], url_path='daily-summary', permission_classes=[IsAuthenticated])
    def daily_summary(self, request):
        date = request.query_params.get('date')
        user_id = request.query_params.get('user_id')

        qs = DietRecord.objects.all()
        if date:
            qs = qs.filter(date=date)
        if user_id:
            qs = qs.filter(user__id=user_id)

        aggregate = qs.aggregate(
            calories=Sum('food__calories'),
            protein=Sum('food__protein'),
            fat=Sum('food__fat'),
            carbs=Sum('food__carbohydrate'),
        )

        for k, v in aggregate.items():
            aggregate[k] = v or 0

        return Response(aggregate, status=status.HTTP_200_OK)


# [6] 로그아웃 (토큰 블랙리스트 등록)
@api_view(['POST'])
@permission_classes([IsAuthenticated])
def logout_user(request):
    try:
        refresh_token = request.data.get("refresh_token")
        if not refresh_token:
            return Response({"detail": "refresh_token is required"}, status=status.HTTP_400_BAD_REQUEST)

        token = RefreshToken(refresh_token)
        token.blacklist()
        return Response({"detail": "로그아웃 완료 (토큰 폐기됨)"}, status=status.HTTP_205_RESET_CONTENT)

    except Exception as e:
        return Response({"detail": f"로그아웃 오류: {str(e)}"}, status=status.HTTP_400_BAD_REQUEST)


# [7] 회원탈퇴 (토큰 블랙리스트 + User, UserInfo 삭제)
@api_view(['DELETE'])
@permission_classes([IsAuthenticated])
def delete_user_account(request):
    try:
        refresh_token = request.data.get("refresh_token")
        if refresh_token:
            try:
                token = RefreshToken(refresh_token)
                token.blacklist()
            except Exception:
                pass  # 이미 만료된 토큰이면 무시

        user = request.user
        UserInfo.objects.filter(user=user).delete()
        user.delete()

        return Response({"detail": "회원 탈퇴 완료 (토큰 및 계정 삭제됨)"}, status=status.HTTP_204_NO_CONTENT)

    except Exception as e:
        return Response({"detail": f"회원탈퇴 오류: {str(e)}"}, status=status.HTTP_400_BAD_REQUEST)
