# goals/views.py

from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from rest_framework import status

from django.utils import timezone
from datetime import timedelta, date

from .models import NutritionGoal, WeightRecord
from foods.models import UserDailyNutrition
from .serializers import (
    NutritionGoalSerializer,
    GoalUpdateSerializer,
    WeightRecordCreateSerializer,
)


# ---------------------------------------------------------
# 📌 1) 목표 자동 생성
# ---------------------------------------------------------
class AutoGoalGenerateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user

        height = user.height or 170
        weight = user.weight or 60
        age = user.age or 25
        gender = user.gender or "male"
        activity = user.activity_level or 1.4

        # BMR (Mifflin–St Jeor)
        if gender == "female":
            bmr = 10 * weight + 6.25 * height - 5 * age - 161
        else:
            bmr = 10 * weight + 6.25 * height - 5 * age + 5

        tdee = bmr * activity

        # 탄단지 추천 분배
        protein = weight * 1.6
        fat = weight * 0.8
        carbs = (tdee - (protein * 4 + fat * 9)) / 4

        goal, created = NutritionGoal.objects.update_or_create(
            user=user,
            defaults={
                "calorie": int(tdee),
                "protein": round(protein, 1),
                "carbs": round(carbs, 1),
                "fat": round(fat, 1),
                "bmr": bmr,
                "activity_level": activity,
            }
        )

        return Response({
            "message": "Goal auto-generated",
            "goal": {
                "calorie": goal.calorie,
                "protein": goal.protein,
                "carbs": goal.carbs,
                "fat": goal.fat,
            }
        }, status=200)


# ---------------------------------------------------------
# 📌 2) 목표 조회
# ---------------------------------------------------------
class GoalRetrieveAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user

        try:
            goal = NutritionGoal.objects.get(user=user)
        except NutritionGoal.DoesNotExist:
            return Response({"detail": "No goal found"}, status=404)

        return Response(NutritionGoalSerializer(goal).data)


# ---------------------------------------------------------
# 📌 3) 목표 수동 수정
# ---------------------------------------------------------
class GoalUpdateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request):
        user = request.user

        goal, created = NutritionGoal.objects.get_or_create(user=user)
        serializer = GoalUpdateSerializer(goal, data=request.data, partial=True)

        if serializer.is_valid():
            serializer.save()
            return Response({"message": "Goal updated"}, status=200)

        return Response(serializer.errors, status=400)


# ---------------------------------------------------------
# 📌 4) 주간 목표 통계 (Placeholder)
# ---------------------------------------------------------
class WeeklyGoalStatAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = date.today()
        start_week = today - timedelta(days=today.weekday())
        end_week = start_week + timedelta(days=6)

        return Response({
            "start_date": str(start_week),
            "end_date": str(end_week),
            "message": "Weekly goal stats (placeholder)"
        })


# ---------------------------------------------------------
# 📌 5) 월간 목표 통계 (Placeholder)
# ---------------------------------------------------------
class MonthlyGoalStatAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = date.today()
        start_month = today.replace(day=1)
        next_month = start_month.replace(month=start_month.month % 12 + 1, day=1)
        end_month = next_month - timedelta(days=1)

        return Response({
            "start_date": str(start_month),
            "end_date": str(end_month),
            "message": "Monthly goal stats (placeholder)"
        })


# ---------------------------------------------------------
# 📌 6) 주간 체중 변화 조회
# ---------------------------------------------------------
class WeeklyWeightView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user

        today = timezone.now().date()
        week_ago = today - timedelta(days=7)

        weights = WeightRecord.objects.filter(
            user=user,
            created_at__date__range=[week_ago, today]
        ).order_by("created_at")

        data = [
            {
                "date": w.created_at.date(),
                "weight": w.weight
            }
            for w in weights
        ]

        return Response({
            "period": f"{week_ago} ~ {today}",
            "records": data
        })


# ---------------------------------------------------------
# 📌 7) 체중 기록 생성
# ---------------------------------------------------------
class WeightRecordCreateView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        serializer = WeightRecordCreateSerializer(data=request.data)
        if serializer.is_valid():
            WeightRecord.objects.create(
                user=request.user,
                weight=serializer.validated_data["weight"]
            )
            return Response({"message": "Weight recorded successfully"}, status=201)

        return Response(serializer.errors, status=400)
