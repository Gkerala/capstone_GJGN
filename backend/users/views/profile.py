from rest_framework.views import APIView
from rest_framework.response import Response
from .models import UserProfile
from .utils.bmr import calculate_bmr, calculate_tdee, auto_calorie_goal

class AutoGoalAPIView(APIView):
    def get(self, request):
        user = request.user
        profile = UserProfile.objects.get(user=user)

        bmr = calculate_bmr(profile.gender, profile.weight, profile.height, profile.age)
        tdee = calculate_tdee(bmr, profile.activity_level)
        target_calories = auto_calorie_goal(tdee, mode=profile.goal_mode)

        return Response({
            "BMR": round(bmr, 2),
            "TDEE": round(tdee, 2),
            "target_calories": round(target_calories, 2),
        })
