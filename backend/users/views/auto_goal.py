from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated

from .models import UserProfile
from .utils.bmr import calculate_bmr, calculate_tdee, auto_calorie_goal
from .utils.macro import calculate_macro_distribution


class AutoGoalSaveAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user
        profile = UserProfile.objects.get(user=user)

        # BMR 계산
        bmr = calculate_bmr(
            profile.gender,
            profile.weight,
            profile.height,
            profile.age
        )
        # TDEE 계산
        tdee = calculate_tdee(bmr, profile.activity_level)

        # 목표 칼로리
        target_kcal = auto_calorie_goal(tdee, profile.goal_mode)

        # 영양소 배분
        macros = calculate_macro_distribution(target_kcal)

        # DB 저장
        profile.goal_calories = round(target_kcal)
        profile.goal_carbs = macros["carbs"]
        profile.goal_protein = macros["protein"]
        profile.goal_fat = macros["fat"]
        profile.save()

        return Response({
            "status": "success",
            "BMR": round(bmr, 2),
            "TDEE": round(tdee, 2),
            "goal": {
                "calories": profile.goal_calories,
                "carbs": profile.goal_carbs,
                "protein": profile.goal_protein,
                "fat": profile.goal_fat,
                "mode": profile.goal_mode
            }
        })
