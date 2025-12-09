# goals/views.py
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

from .models import UserGoal
from .serializers import (
    UserGoalSerializer,
    UserGoalUpdateSerializer,
)
from foods.models import UserDailyNutrition


# --------------------------------------------------
# 🔥 utils.py 제거 → 계산 함수 직접 선언
# --------------------------------------------------

def get_activity_factor(activity):
    factors = {
        1: 1.2,
        2: 1.375,
        3: 1.55,
        4: 1.725,
        5: 1.9,
    }
    return factors.get(activity, 1.2)


def calculate_daily_targets(gender, weight, height, age, activity, goal_type):
    # --- 1. BMR ---
    if gender == "male":
        bmr = 10 * weight + 6.25 * height - 5 * age + 5
    else:
        bmr = 10 * weight + 6.25 * height - 5 * age - 161

    # --- 2. TDEE ---
    tdee = bmr * get_activity_factor(activity)

    # --- 3. 목표 유형 적용 ---
    if goal_type == 1:        # 감량
        tdee *= 0.85
    elif goal_type == 3:      # 증량
        tdee *= 1.15

    tdee = max(tdee, 1200)

    # --- 4. 탄수화물 / 단백질 / 지방 ---
    carbs = (tdee * 0.50) / 4
    protein = (tdee * 0.20) / 4
    fat = (tdee * 0.30) / 9

    # --- 5. 당 (WHO 10%) ---
    sugar = (tdee * 0.10) / 4

    return {
        "tdee": round(tdee),
        "carbs": round(carbs),
        "protein": round(protein),
        "fat": round(fat),
        "sugar": round(sugar),
    }


# ----------------------------------------------------
# 0) UserGoal 조회
# ----------------------------------------------------
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

        serializer.save()

        # 사용자 정보
        height = user.height or 170
        weight = user.weight or 60
        age = user.age or 25
        gender = user.gender or "male"
        activity = goal.activity_level or 3
        goal_type = goal.goal_type or 2

        # 🔥 계산 실행
        daily = calculate_daily_targets(
            gender=gender,
            weight=weight,
            height=height,
            age=age,
            activity=activity,
            goal_type=goal_type,
        )

        # DB 저장
        goal.kcal = daily["tdee"]
        goal.carbs = daily["carbs"]
        goal.protein = daily["protein"]
        goal.fat = daily["fat"]
        goal.sugar = daily["sugar"]

        goal.save()


        return Response({
            "message": "User goal updated.",
            "goal": UserGoalSerializer(goal).data
        })


# ----------------------------------------------------
# 2) 자동 목표 생성 (target_* 제거 버전)
# ----------------------------------------------------
class AutoGoalGenerateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user
        goal, _ = UserGoal.objects.get_or_create(user=user)

        # 사용자 정보
        height = user.height or 170
        weight = user.weight or 60
        age = user.age or 25
        gender = user.gender or "male"
        activity = goal.activity_level or 3
        goal_type = goal.goal_type or 2

        # 🔥 목표 계산
        daily = calculate_daily_targets(
            gender=gender,
            weight=weight,
            height=height,
            age=age,
            activity=activity,
            goal_type=goal_type,
        )

        # 🔥 계산 결과를 실제 목표 필드에 저장
        goal.kcal = daily["tdee"]
        goal.carbs = daily["carbs"]
        goal.protein = daily["protein"]
        goal.fat = daily["fat"]
        goal.sugar = daily["sugar"]

        goal.save()

        return Response({
            "message": "Goal auto-generated",
            "goal": UserGoalSerializer(goal).data
        })

