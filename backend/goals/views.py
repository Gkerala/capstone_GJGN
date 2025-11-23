# goals/views.py
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

from .models import UserGoal, WeightRecord
from .serializers import (
    UserGoalSerializer,
    UserGoalUpdateSerializer,
    WeightRecordCreateSerializer
)
from .utils import calculate_daily_targets   # ← 핵심
from foods.models import UserDailyNutrition



# 0) UserGoal 조회
class UserGoalRetrieveAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        goal, _ = UserGoal.objects.get_or_create(user=request.user)
        return Response(UserGoalSerializer(goal).data)



# ----------------------------------------------------
# 1) UserGoal 수정 + 자동 영양목표 계산
# ----------------------------------------------------
class UserGoalUpdateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request):
        user = request.user
        goal, _ = UserGoal.objects.get_or_create(user=user)

        serializer = UserGoalUpdateSerializer(goal, data=request.data, partial=True)

        if not serializer.is_valid():
            return Response(serializer.errors, status=400)

        serializer.save()  # goal_type, goal_weight, activity_level 저장

        # -----------------------------------------
        # 🔥 사용자 정보 기반 자동 목표 계산
        # -----------------------------------------
        height = user.height or 170
        weight = user.weight or 60
        age = user.age or 25
        gender = user.gender or "male"
        activity = goal.activity_level or 3

        daily = calculate_daily_targets(
            gender=gender,
            weight=weight,
            height=height,
            age=age,
            activity=activity,
            goal_type=goal.goal_type, 
        )

        # -----------------------------------------
        # 🔥 계산된 목표 UserGoal에 저장
        # -----------------------------------------
        goal.kcal = daily["tdee"]
        goal.carbs = daily["carbs"]
        goal.protein = daily["protein"]
        goal.fat = daily["fat"]
        goal.sugar = daily["sugar"]     # ← 추가됨

        goal.save()

        return Response({
            "message": "User goal updated with auto-calculated nutrition.",
            "goal": UserGoalSerializer(goal).data
        }, status=200)



# ----------------------------------------------------
# 2) 자동 목표 생성 (앱에서 최초 설정 시 사용)
# ----------------------------------------------------
class AutoGoalGenerateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user
        goal, _ = UserGoal.objects.get_or_create(user=user)

        height = user.height or 170
        weight = user.weight or 60
        age = user.age or 25
        gender = user.gender or "male"
        activity = goal.activity_level or 3

        daily = calculate_daily_targets(
            gender=gender,
            weight=weight,
            height=height,
            age=age,
            activity=activity,
        )

        # target_* 필드 저장
        goal.target_kcal = daily["tdee"]
        goal.target_carb = daily["carbs"]
        goal.target_protein = daily["protein"]
        goal.target_fat = daily["fat"]
        goal.target_sugar = daily["sugar"]   # ← sugar 저장 추가

        goal.save()

        return Response({
            "message": "Goal auto-generated",
            "goal": UserGoalSerializer(goal).data
        })
