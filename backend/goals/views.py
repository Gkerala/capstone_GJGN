from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from django.utils import timezone
from datetime import timedelta, date

from .models import UserGoal, WeightRecord
from .serializers import (
    UserGoalSerializer,
    UserGoalUpdateSerializer,
    WeightRecordCreateSerializer
)
from foods.models import UserDailyNutrition


# 0) UserGoal 조회
class UserGoalRetrieveAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        goal, _ = UserGoal.objects.get_or_create(user=request.user)
        print("DEBUG GOAL RESPONSE:", UserGoalSerializer(goal).data)
        return Response(UserGoalSerializer(goal).data)


# ----------------------------------------------------
# 1) UserGoal 수정 (목표 유형 / 목표 체중 / 활동량) + 자동 칼로리 계산
# ----------------------------------------------------
class UserGoalUpdateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request):
        user = request.user
        goal, _ = UserGoal.objects.get_or_create(user=user)

        serializer = UserGoalUpdateSerializer(goal, data=request.data, partial=True)

        if serializer.is_valid():
            serializer.save()   # 1차 저장 (goal_type / goal_weight / activity_level 저장)

            # -----------------------------------------------------
            # 🔥 자동 칼로리 계산 (TDEE 기반)
            # -----------------------------------------------------
            height = user.height or 170
            weight = user.weight or 60
            age = user.age or 25
            gender = user.gender or "male"

            activity = goal.activity_level or 3

            activity_factor_map = {
                1: 1.2,
                2: 1.375,
                3: 1.55,
                4: 1.725,
                5: 1.9,
            }

            activity_factor = activity_factor_map.get(activity, 1.55)

            # BMR 계산
            if gender == "female":
                bmr = 10 * weight + 6.25 * height - 5 * age - 161
            else:
                bmr = 10 * weight + 6.25 * height - 5 * age + 5

            tdee = bmr * activity_factor

            # 탄단지 계산
            protein = weight * 1.6
            fat = weight * 0.8
            carbs = (tdee - (protein * 4 + fat * 9)) / 4

            # -----------------------------------------------------
            # 🔥 자동 계산된 목표를 UserGoal에 저장
            # -----------------------------------------------------
            goal.kcal = int(tdee)
            goal.protein = round(protein, 1)
            goal.fat = round(fat, 1)
            goal.carbs = round(carbs, 1)
            goal.auto_mode = False
            goal.save()

            return Response({
                "message": "User goal updated with auto-calculated nutrition.",
                "goal": UserGoalSerializer(goal).data
            }, status=200)

        return Response(serializer.errors, status=400)



# 2) UserGoal 기반 자동 목표 생성
class AutoGoalGenerateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user
        goal, _ = UserGoal.objects.get_or_create(user=user)

        height = user.height or 170
        weight = user.weight or 60
        age = user.age or 25
        gender = user.gender or "male"

        activity_factor_map = {
            1: 1.2,
            2: 1.375,
            3: 1.55,
            4: 1.725,
            5: 1.9,
        }
        activity_factor = activity_factor_map.get(goal.activity_level, 1.55)

        # BMR
        if gender == "female":
            bmr = 10 * weight + 6.25 * height - 5 * age - 161
        else:
            bmr = 10 * weight + 6.25 * height - 5 * age + 5

        tdee = bmr * activity_factor

        # 탄단지 계산
        protein = weight * 1.6
        fat = weight * 0.8
        carbs = (tdee - (protein * 4 + fat * 9)) / 4

        goal.target_kcal = int(tdee)
        goal.target_protein = int(protein)
        goal.target_fat = int(fat)
        goal.target_carb = int(carbs)
        goal.save()

        return Response({
            "message": "Goal auto-generated",
            "goal": UserGoalSerializer(goal).data
        })
