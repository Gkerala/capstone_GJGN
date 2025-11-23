# backend/goals/views.py

from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

from .models import UserGoal, WeightRecord
from .serializers import (
    UserGoalSerializer,
    UserGoalUpdateSerializer,
    WeightRecordCreateSerializer,
)

from goals.utils import calculate_daily_targets  # ✅ 여기 추가됨


# ----------------------------------------------------
# 0) UserGoal 조회
# ----------------------------------------------------
class UserGoalRetrieveAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        goal, _ = UserGoal.objects.get_or_create(user=request.user)
        return Response(UserGoalSerializer(goal).data)


# ----------------------------------------------------
# 1) UserGoal 수정 + 자동 칼로리/탄단지/설탕 계산
# ----------------------------------------------------
class UserGoalUpdateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request):
        user = request.user
        goal, _ = UserGoal.objects.get_or_create(user=user)

        serializer = UserGoalUpdateSerializer(goal, data=request.data, partial=True)

        if serializer.is_valid():
            serializer.save()  # goal_type, goal_weight, activity_level 저장

            # 사용자 기본 정보
            height = user.height or 170
            weight = user.weight or 60
            age = user.age or 25
            gender = user.gender or "male"
            activity = goal.activity_level or 3

            # -----------------------------------------------------
            # 🔥 utils.py 기반 자동 계산
            # -----------------------------------------------------
            targets = calculate_daily_targets(
                gender=gender,
                weight=weight,
                height=height,
                age=age,
                activity=activity
            )

            # -----------------------------------------------------
            # 🔥 UserGoal 업데이트
            # -----------------------------------------------------
            goal.kcal = targets["tdee"]
            goal.carbs = targets["carbs"]
            goal.protein = targets["protein"]
            goal.fat = targets["fat"]
            goal.sugar = targets["sugar"]     # ← 신규 필드 사용 시 자동 저장
            goal.auto_mode = False
            goal.save()

            return Response({
                "message": "User goal updated with auto-calculated nutrition.",
                "goal": UserGoalSerializer(goal).data
            })

        return Response(serializer.errors, status=400)


# ----------------------------------------------------
# 2) UserGoal 기반 자동 목표 생성 (auto mode)
# ----------------------------------------------------
class AutoGoalGenerateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user
        goal, _ = UserGoal.objects.get_or_create(user=user)

        # 사용자 값
        height = user.height or 170
        weight = user.weight or 60
        age = user.age or 25
        gender = user.gender or "male"
        activity = goal.activity_level or 3

        # -----------------------------------------------------
        # 🔥 utils.py 자동 계산 호출
        # -----------------------------------------------------
        targets = calculate_daily_targets(
            gender=gender,
            weight=weight,
            height=height,
            age=age,
            activity=activity
        )

        # -----------------------------------------------------
        # 🔥 UserGoal 저장 (auto mode)
        # -----------------------------------------------------
        goal.target_kcal = targets["tdee"]
        goal.target_carb = targets["carbs"]
        goal.target_protein = targets["protein"]
        goal.target_fat = targets["fat"]
        goal.target_sugar = targets["sugar"]
        goal.save()

        return Response({
            "message": "Goal auto-generated",
            "goal": UserGoalSerializer(goal).data
        })
