from datetime import datetime
from django.db.models import Sum
from foods.models import Food
from datetime import timedelta
from django.utils import timezone
from records.models import Record, Weight

class RecordService:

    @staticmethod
    def get_daily_stats(user, target_date=None):
        """
        하루 영양 통계 계산 서비스
        """
        if target_date is None:
            target_date = datetime.today().date()

        records = Record.objects.filter(user=user, eaten_at__date=target_date)

        if not records.exists():
            return {
                "date": target_date,
                "total_calories": 0,
                "total_carbs": 0,
                "total_protein": 0,
                "total_fat": 0,
                "top_foods": [],
                "macro_ratio": {"carbs": 0, "protein": 0, "fat": 0}
            }

        # 영양 합계
        totals = records.aggregate(
            total_calories=Sum("food__calories"),
            total_carbs=Sum("food__carbs"),
            total_protein=Sum("food__protein"),
            total_fat=Sum("food__fat"),
        )

        # 섭취 음식 TOP3
        top_foods = (
            records.values("food__name")
            .annotate(count=Sum("amount"))
            .order_by("-count")[:3]
        )

        # 비율 계산
        total = totals["total_carbs"] + totals["total_protein"] + totals["total_fat"]
        if total > 0:
            macro_ratio = {
                "carbs": round(totals["total_carbs"] / total * 100, 1),
                "protein": round(totals["total_protein"] / total * 100, 1),
                "fat": round(totals["total_fat"] / total * 100, 1),
            }
        else:
            macro_ratio = {"carbs": 0, "protein": 0, "fat": 0}

        return {
            "date": target_date,
            "total_calories": totals["total_calories"],
            "total_carbs": totals["total_carbs"],
            "total_protein": totals["total_protein"],
            "total_fat": totals["total_fat"],
            "top_foods": list(top_foods),
            "macro_ratio": macro_ratio,
        }

class AnalysisService:

    @staticmethod
    def weekly_summary(user):
        today = timezone.now().date()
        start_date = today - timedelta(days=6)

        # 날짜별 초기 구조
        result = {}
        for i in range(7):
            day = start_date + timedelta(days=i)
            result[str(day)] = {
                "calories": 0,
                "carbs": 0,
                "protein": 0,
                "fat": 0
            }

        # 식단 기록
        records = Record.objects.filter(
            user=user,
            created_at__date__range=[start_date, today]
        )

        for r in records:
            day = str(r.created_at.date())
            result[day]["calories"] += r.food.calories * r.amount
            result[day]["carbs"] += r.food.carbs * r.amount
            result[day]["protein"] += r.food.protein * r.amount
            result[day]["fat"] += r.food.fat * r.amount

        # 체중 기록
        weights = Weight.objects.filter(
            user=user,
            created_at__date__range=[start_date, today]
        ).order_by("created_at")

        weight_list = [{
            "date": str(w.created_at.date()),
            "weight": w.weight
        } for w in weights]

        return {
            "start_date": str(start_date),
            "end_date": str(today),
            "daily": result,
            "weights": weight_list
        }
        
    @staticmethod
    def monthly_summary(user):
        today = timezone.now().date()
        start_date = today - timedelta(days=29)

        records = Record.objects.filter(
            user=user,
            created_at__date__range=[start_date, today]
        )

        # 날짜별 칼로리
        date_map = {}
        for i in range(30):
            date_map[str(start_date + timedelta(days=i))] = 0

        for r in records:
            date = str(r.created_at.date())
            date_map[date] += r.food.calories * r.amount

        # 7일 단위(4주 평균)
        weeks = []
        date_list = list(date_map.values())

        for i in range(0, 30, 7):
            week_data = date_list[i:i+7]
            avg = sum(week_data) / len(week_data)
            weeks.append(avg)

        # 체중 (평균)
        weights = Weight.objects.filter(
            user=user,
            created_at__date__range=[start_date, today]
        )

        weight_list = [w.weight for w in weights]
        avg_weight = sum(weight_list) / len(weight_list) if weight_list else None

        return {
            "start_date": str(start_date),
            "end_date": str(today),
            "weekly_avg_calories": weeks,
            "monthly_avg_weight": avg_weight
        }

