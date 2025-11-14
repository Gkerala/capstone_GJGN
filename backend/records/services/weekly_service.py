from datetime import timedelta, datetime
from django.utils.timezone import make_aware

from records.models import MealRecord
from users.models import UserGoal


class WeeklyStatService:

    @staticmethod
    def get_weekly_stats(user):
        today = make_aware(datetime.now()).date()
        start_date = today - timedelta(days=6)

        records = MealRecord.objects.filter(
            user=user,
            eaten_at__date__range=(start_date, today)
        )

        goal = UserGoal.objects.get(user=user)

        # 날짜별 합산
        daily_summary = {}
        for i in range(7):
            d = start_date + timedelta(days=i)
            daily_summary[str(d)] = {
                "eat_kcal": 0,
                "target_kcal": goal.target_kcal,
                "kcal_percent": 0,
            }

        for r in records:
            d = str(r.eaten_at.date())
            daily_summary[d]["eat_kcal"] += r.total_kcal

        # % 계산
        for d, v in daily_summary.items():
            if v["target_kcal"] > 0:
                v["kcal_percent"] = round(v["eat_kcal"] / v["target_kcal"] * 100, 1)

        # 주간 평균
        avg_percent = round(
            sum(v["kcal_percent"] for v in daily_summary.values()) / 7, 1
        )

        return {
            "start_date": str(start_date),
            "end_date": str(today),
            "daily": daily_summary,
            "weekly_avg_percent": avg_percent,
        }
