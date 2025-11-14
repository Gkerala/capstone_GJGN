from datetime import date, datetime, timedelta

from django.db.models import Sum
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated

from users.models import User
from foods.models import UserDailyNutrition
from .models import UserGoal


def calculate_achievement_rate(total_intake, target_value):
    """
    달성률 계산 함수
    - 섭취량이 목표보다 많아도 100% 초과 불가
    """
    if target_value == 0:
        return 0
    
    rate = (total_intake / target_value) * 100
    return round(min(rate, 100), 2)


class GoalSummaryView(APIView):
    """
    GET /api/goals/summary/?date=2025-11-14
    사용자의 하루 목표 달성률 요약 API
    """
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user: User = request.user

        # 조회 날짜
        target_date_str = request.query_params.get("date", None)
        if target_date_str:
            try:
                target_date = datetime.strptime(target_date_str, "%Y-%m-%d").date()
            except ValueError:
                return Response({"error": "Invalid date format. Use YYYY-MM-DD."}, status=400)
        else:
            target_date = date.today()

        # 1) 최신 UserGoal 가져오기
        try:
            user_goal: UserGoal = UserGoal.objects.get(user=user)
        except UserGoal.DoesNotExist:
            return Response({"message": "User goal not set."}, status=404)

        # 2) 하루 섭취량 합계 가져오기
        nutrition = (
            UserDailyNutrition.objects.filter(user=user, date=target_date)
            .aggregate(
                total_cal=Sum("calories"),
                total_carbs=Sum("carbs"),
                total_protein=Sum("protein"),
                total_fat=Sum("fat"),
            )
        )

        # None → 0 치환
        total_cal = nutrition["total_cal"] or 0
        total_carbs = nutrition["total_carbs"] or 0
        total_protein = nutrition["total_protein"] or 0
        total_fat = nutrition["total_fat"] or 0

        # 3) 달성률 계산
        achievement = {
            "cal": calculate_achievement_rate(total_cal, user_goal.daily_calorie_goal),
            "carbs": calculate_achievement_rate(total_carbs, user_goal.daily_carbs_goal),
            "protein": calculate_achievement_rate(total_protein, user_goal.daily_protein_goal),
            "fat": calculate_achievement_rate(total_fat, user_goal.daily_fat_goal),
        }

        # 4) 전체 평균 달성률
        overall_achievement = round(
            (achievement["cal"] + achievement["carbs"] + achievement["protein"] + achievement["fat"]) / 4, 2
        )

        return Response(
            {
                "date": target_date,
                "intake": {
                    "calories": total_cal,
                    "carbs": total_carbs,
                    "protein": total_protein,
                    "fat": total_fat,
                },
                "goal": {
                    "calories": user_goal.daily_calorie_goal,
                    "carbs": user_goal.daily_carbs_goal,
                    "protein": user_goal.daily_protein_goal,
                    "fat": user_goal.daily_fat_goal,
                },
                "achievement_rate": achievement,
                "overall_achievement": overall_achievement,
            },
            status=200,
        )


class WeeklyGoalSummaryView(APIView):
    """
    GET /api/goals/weekly/?start=2025-11-10
    주간 목표 달성률 API
    """
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user

        # 시작 날짜
        start_str = request.query_params.get("start")
        if not start_str:
            return Response({"error": "start=YYYY-MM-DD required"}, status=400)
        
        try:
            start_date = datetime.strptime(start_str, "%Y-%m-%d").date()
        except ValueError:
            return Response({"error": "Invalid date format."}, status=400)

        end_date = start_date + timedelta(days=6)

        try:
            user_goal = UserGoal.objects.get(user=user)
        except UserGoal.DoesNotExist:
            return Response({"message": "User goal not set."}, status=404)

        # 요약 저장 배열
        week_data = []

        for offset in range(7):
            day = start_date + timedelta(days=offset)

            daily = (
                UserDailyNutrition.objects.filter(user=user, date=day)
                .aggregate(
                    cal=Sum("calories"),
                    carbs=Sum("carbs"),
                    protein=Sum("protein"),
                    fat=Sum("fat"),
                )
            )

            total_cal = daily["cal"] or 0
            total_carbs = daily["carbs"] or 0
            total_protein = daily["protein"] or 0
            total_fat = daily["fat"] or 0

            achievement = {
                "cal": calculate_achievement_rate(total_cal, user_goal.daily_calorie_goal),
                "carbs": calculate_achievement_rate(total_carbs, user_goal.daily_carbs_goal),
                "protein": calculate_achievement_rate(total_protein, user_goal.daily_protein_goal),
                "fat": calculate_achievement_rate(total_fat, user_goal.daily_fat_goal),
            }

            overall = round(
                (achievement["cal"] + achievement["carbs"] + achievement["protein"] + achievement["fat"]) / 4,
                2,
            )

            week_data.append(
                {
                    "date": day,
                    "intake": {
                        "calories": total_cal,
                        "carbs": total_carbs,
                        "protein": total_protein,
                        "fat": total_fat,
                    },
                    "achievement_rate": achievement,
                    "overall": overall,
                }
            )

        return Response(
            {
                "start": start_date,
                "end": end_date,
                "weekly_summary": week_data,
            },
            status=200,
        )
