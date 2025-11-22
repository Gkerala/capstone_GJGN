# goals/views.py
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status

from datetime import timedelta, date
from django.utils import timezone

from .models import NutritionGoal, WeightRecord
from foods.models import UserDailyNutrition
from .serializers import (
    NutritionGoalSerializer,
    GoalUpdateSerializer,
    WeightRecordCreateSerializer,
)


# -------------------------------
# 1) 목표 자동 생성
# -------------------------------
class AutoGoalGenerateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user

        height = user.height or 170
        weight = user.weight or 60
        age = user.age or 25
        gender = user.gender or "male"
        activity = user.activity_level or 1.4

        # BMR
        if gender == "female":
            bmr = 10 * weight + 6.25 * height - 5 * age - 161
        else:
            bmr = 10 * weight + 6.25 * height - 5 * age + 5

        tdee = bmr * float(activity)

        # 탄단지 분배
        protein = weight * 1.6
        fat = weight * 0.8
        carbs = (tdee - (protein * 4 + fat * 9)) / 4

        goal, _ = NutritionGoal.objects.update_or_create(
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
            "goal": NutritionGoalSerializer(goal).data
        }, status=200)


# -------------------------------
# 2) 목표 조회
# -------------------------------
class GoalRetrieveAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user

        try:
            goal = NutritionGoal.objects.get(user=user)
        except NutritionGoal.DoesNotExist:
            return Response({"detail": "Goal not found"}, status=404)

        return Response(NutritionGoalSerializer(goal).data)


# -------------------------------
# 3) 목표 수동 수정
# -------------------------------
class GoalUpdateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request):
        user = request.user
        goal, _ = NutritionGoal.objects.get_or_create(user=user)

        serializer = GoalUpdateSerializer(goal, data=request.data, partial=True)

        if serializer.is_valid():
            serializer.save()
            return Response({
                "message": "Goal updated",
                "goal": NutritionGoalSerializer(goal).data
            })

        return Response(serializer.errors, status=400)


# -------------------------------
# 4) 주간 섭취 요약 (영양 섭취량)
# -------------------------------
class WeeklyGoalStatAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        today = date.today()
        start = today - timedelta(days=6)

        nutrition = UserDailyNutrition.objects.filter(
            user=user,
            date__range=[start, today]
        ).order_by("date")

        result = [
            {
                "date": str(n.date),
                "calorie": n.calorie,
                "carbs": n.carbs,
                "protein": n.protein,
                "fat": n.fat,
            }
            for n in nutrition
        ]

        return Response({
            "start_date": str(start),
            "end_date": str(today),
            "data": result
        })


# -------------------------------
# 5) 월간 섭취 요약
# -------------------------------
class MonthlyGoalStatAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        today = date.today()
        start = today.replace(day=1)
        end = today

        nutrition = UserDailyNutrition.objects.filter(
            user=user,
            date__range=[start, end]
        ).order_by("date")

        result = [
            {
                "date": str(n.date),
                "calorie": n.calorie,
                "carbs": n.carbs,
                "protein": n.protein,
                "fat": n.fat,
            }
            for n in nutrition
        ]

        return Response({
            "start_date": str(start),
            "end_date": str(end),
            "data": result
        })


# -------------------------------
# 6) 주간 체중 변화 조회
# -------------------------------
class WeeklyWeightView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        today = timezone.now().date()
        start = today - timedelta(days=6)

        records = WeightRecord.objects.filter(
            user=user,
            created_at__date__range=[start, today]
        ).order_by("created_at")

        result = [
            {
                "date": w.created_at.date(),
                "weight": w.weight
            }
            for w in records
        ]

        return Response({
            "start_date": str(start),
            "end_date": str(today),
            "records": result
        })


# -------------------------------
# 7) 체중 기록 추가
# -------------------------------
class WeightRecordCreateView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        serializer = WeightRecordCreateSerializer(data=request.data)

        if serializer.is_valid():
            weight = serializer.validated_data["weight"]

            WeightRecord.objects.create(
                user=request.user,
                weight=weight,
                created_at=timezone.now()
            )

            return Response({"message": "Weight record saved"}, status=201)

        return Response(serializer.errors, status=400)
