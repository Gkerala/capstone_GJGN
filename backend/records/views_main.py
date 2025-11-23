from datetime import datetime
from django.utils.timezone import now
from rest_framework.response import Response
from rest_framework import generics, permissions
from django.db.models.functions import Cast
from django.db.models import DateField
from records.models import MealRecord, MealFood, WeightRecord
from goals.models import UserGoal


class MainSummaryAPIView(generics.GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        user = request.user

        # ---------------------------
        # 0) 날짜 파싱
        # ---------------------------
        date_str = request.GET.get("date")

        if date_str:
            try:
                today = datetime.strptime(date_str, "%Y-%m-%d").date()
            except ValueError:
                return Response({"error": "잘못된 날짜 형식 (YYYY-MM-DD)"}, status=400)
        else:
            today = now().date()

        print("📅 조회 날짜:", today)

        # ---------------------------
        # 1) Goal (UserGoal)
        # ---------------------------
        goal = UserGoal.objects.filter(user=user).first()

        # 🔥 target_* 제거 후 실제 저장 필드 사용
        goal_kcal = goal.kcal if goal else 2000
        goal_carb = goal.carbs if goal else 250
        goal_protein = goal.protein if goal else 120
        goal_fat = goal.fat if goal else 60
        goal_sugar = goal.sugar if goal else 50

        # ---------------------------
        # 2) Today Meal Records
        # ---------------------------
        meal_records = (
            MealRecord.objects.annotate(
                meal_date=Cast("meal_time", DateField())
            )
            .filter(user=user, meal_date=today)
            .order_by("meal_time")
        )

        total = {"kcal": 0, "carb": 0, "protein": 0, "fat": 0, "sugar": 0}
        meal_summary = {"breakfast": None, "lunch": None, "dinner": None}

        for rec in meal_records:
            foods = MealFood.objects.filter(record_id=rec.id)

            sum_info = {
                "kcal": sum(f.kcal for f in foods),
                "carb": sum(f.carb for f in foods),
                "protein": sum(f.protein for f in foods),
                "fat": sum(f.fat for f in foods),
                "sugar": sum(f.sugar for f in foods),
            }

            for k in total:
                total[k] += sum_info[k]

            meal_type = (rec.meal_type or "").lower()
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

        # ---------------------------
        # 3) Weight Info
        # ---------------------------
        first_weight = WeightRecord.objects.filter(user=user).order_by("date").first()
        today_weight = WeightRecord.objects.filter(user=user, date=today).first()

        weight_data = {
            "start_weight": first_weight.weight if first_weight else None,
            "today_weight": today_weight.weight if today_weight else (
                WeightRecord.objects.filter(user=user).order_by("-date").first().weight
                if WeightRecord.objects.filter(user=user).exists()
                else None
            ),
        }

        # ---------------------------
        # 4) Percent 계산
        # ---------------------------
        percent = lambda val, goal: round((val / goal) * 100, 1) if goal > 0 else 0

        today_data = {
            "total_kcal": total["kcal"],
            "goal_kcal": goal_kcal,
            "kcal_percent": percent(total["kcal"], goal_kcal),

            "carb": total["carb"],
            "goal_carb": goal_carb,
            "carb_percent": percent(total["carb"], goal_carb),

            "protein": total["protein"],
            "goal_protein": goal_protein,
            "protein_percent": percent(total["protein"], goal_protein),

            "fat": total["fat"],
            "goal_fat": goal_fat,
            "fat_percent": percent(total["fat"], goal_fat),

            "sugar": total["sugar"],
            "goal_sugar": goal_sugar,
            "sugar_percent": percent(total["sugar"], goal_sugar),
        }

        return Response({
            "today": today_data,
            "weight": weight_data,
            "meals": meal_summary,
        })
