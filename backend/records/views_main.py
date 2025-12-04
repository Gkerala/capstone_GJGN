from datetime import datetime, time, timedelta
from django.utils import timezone
from django.db.models.functions import Cast
from django.db.models import DateField, Sum
from rest_framework.response import Response
from rest_framework import generics, permissions

from records.models import MealRecord, MealFood, WeightRecord
from goals.models import UserGoal


def get_kst_range(date_kst):
    """
    KST 00:00:00 ~ 23:59:59 를 UTC 로 변환하여 반환
    """
    KST = timezone.get_current_timezone()  # Asia/Seoul (ZoneInfo)

    # naive datetime 구성
    start_kst = datetime.combine(date_kst, time.min)
    end_kst = datetime.combine(date_kst, time.max)

    # aware 로 변환
    start_kst = timezone.make_aware(start_kst, KST)
    end_kst = timezone.make_aware(end_kst, KST)

    # UTC 로 변환
    start_utc = start_kst.astimezone(timezone.utc)
    end_utc = end_kst.astimezone(timezone.utc)

    return start_utc, end_utc



class MainSummaryAPIView(generics.GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        user = request.user

        # -------------------------------------
        # 1) 요청 날짜 파싱 (KST 기준)
        # -------------------------------------
        date_str = request.GET.get("date")

        if date_str:
            try:
                today_kst = datetime.strptime(date_str, "%Y-%m-%d").date()
            except ValueError:
                return Response({"error": "날짜 형식 오류 (YYYY-MM-DD)"}, status=400)
        else:
            today_kst = timezone.now().astimezone(
                timezone.get_current_timezone()
            ).date()

        print("📅 [KST 기준 조회 날짜]:", today_kst)

        # 🟦 조회 KST 날짜 → UTC 범위 변환
        start_utc, end_utc = get_kst_range(today_kst)
        print("🕒 KST→UTC RANGE:", start_utc, "~", end_utc)

        # =====================================
        # 2) 목표 데이터
        # =====================================
        goal = UserGoal.objects.filter(user=user).first()

        goal_values = {
            "kcal": goal.kcal if goal else 2000,
            "carb": goal.carbs if goal else 250,
            "protein": goal.protein if goal else 120,
            "fat": goal.fat if goal else 60,
            "sugar": goal.sugar if goal else 50,
            "goal_weight": goal.goal_weight if goal else None,
        }

        # =====================================
        # 3) 식단 데이터 (UTC → KST 변환 후 date 비교)
        # =====================================
        meal_records = MealRecord.objects.filter(
            user=user,
            meal_time__range=(start_utc, end_utc)
        ).order_by("meal_time")

        meal_summary = {"breakfast": None, "lunch": None, "dinner": None}
        total = {"kcal": 0, "carb": 0, "protein": 0, "fat": 0, "sugar": 0}

        for record in meal_records:
            foods = MealFood.objects.filter(record_id=record.id)

            sum_info = {
                "kcal": sum(f.kcal for f in foods),
                "carb": sum(f.carb for f in foods),
                "protein": sum(f.protein for f in foods),
                "fat": sum(f.fat for f in foods),
                "sugar": sum(f.sugar for f in foods),
            }

            # 총합 누적
            for key in total:
                total[key] += sum_info[key]

            meal_type = record.meal_type.lower()
            if meal_type not in meal_summary:
                meal_type = "breakfast"

            meal_summary[meal_type] = {
                "foods": [
                    {
                        "name": f.food_name,
                        "kcal": f.kcal,
                        "carb": f.carb,
                        "protein": f.protein,
                        "fat": f.fat,
                        "sugar": f.sugar,
                    }
                    for f in foods
                ],
                "total": sum_info,
            }

        # =====================================
        # 4) 체중 정보 (UTC → KST 기준 조회)
        # =====================================
        first_weight = WeightRecord.objects.filter(user=user).order_by("created_at").first()
        today_weight_obj = WeightRecord.objects.filter(
            user=user,
            created_at__range=(start_utc, end_utc)
        ).order_by("-created_at").first()

        weight_data = {
            "start_weight": first_weight.weight if first_weight else None,
            "today_weight": today_weight_obj.weight if today_weight_obj else None,
            "goal_weight": goal_values["goal_weight"],
        }

        # =====================================
        # 5) 비율 계산
        # =====================================
        def pct(val, goal):
            return round((val / goal) * 100, 1) if goal > 0 else 0

        today_data = {
            "total_kcal": total["kcal"],
            "goal_kcal": goal_values["kcal"],
            "kcal_percent": pct(total["kcal"], goal_values["kcal"]),

            "carb": total["carb"],
            "goal_carb": goal_values["carb"],
            "carb_percent": pct(total["carb"], goal_values["carb"]),

            "protein": total["protein"],
            "goal_protein": goal_values["protein"],
            "protein_percent": pct(total["protein"], goal_values["protein"]),

            "fat": total["fat"],
            "goal_fat": goal_values["fat"],
            "fat_percent": pct(total["fat"], goal_values["fat"]),

            "sugar": total["sugar"],
            "goal_sugar": goal_values["sugar"],
            "sugar_percent": pct(total["sugar"], goal_values["sugar"]),
        }

        # =====================================
        # 최종 응답
        # =====================================
        return Response({
            "today": today_data,
            "weight": weight_data,
            "meals": meal_summary,
        })
