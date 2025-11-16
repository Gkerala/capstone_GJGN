from datetime import datetime
from django.utils.timezone import make_aware
from records.models import MealRecord
from users.models import UserGoal


class DailyStatService:

    @staticmethod
    def get_today_stats(user):
        today = make_aware(datetime.now()).date()

        records = MealRecord.objects.filter(user=user, eaten_at__date=today)

        total_kcal = sum(r.total_kcal for r in records)
        total_carb = sum(r.total_carb for r in records)
        total_protein = sum(r.total_protein for r in records)
        total_fat = sum(r.total_fat for r in records)

        # 목표 불러오기
        goal = UserGoal.objects.get(user=user)

        def percent(eat, target):
            if target == 0:
                return 0
            return round((eat / target) * 100, 1)

        return {
            "date": str(today),
            "eat_kcal": total_kcal,
            "target_kcal": goal.target_kcal,
            "kcal_percent": percent(total_kcal, goal.target_kcal),

            "eat_carb": total_carb,
            "carb_percent": percent(total_carb, goal.target_carb),

            "eat_protein": total_protein,
            "protein_percent": percent(total_protein, goal.target_protein),

            "eat_fat": total_fat,
            "fat_percent": percent(total_fat, goal.target_fat),
        }
