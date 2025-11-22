# backend/records/views.py
from datetime import timedelta
from django.db.models import Sum
from django.utils.timezone import now

from rest_framework import generics, status
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

from .models import MealRecord
from .serializers import (
    MealRecordCreateSerializer,
    MealRecordSerializer,
    MealRecordListSerializer,
    MealRecordDetailSerializer,
)


# ------------------------------------------
# 1) 식단 기록 저장 API
# ------------------------------------------
class MealRecordCreateAPIView(generics.CreateAPIView):
    serializer_class = MealRecordCreateSerializer
    permission_classes = [IsAuthenticated]

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)


# ------------------------------------------
# 2) 날짜별 또는 전체 리스트 조회
# ------------------------------------------
class MealRecordListAPIView(generics.ListAPIView):
    serializer_class = MealRecordListSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        query_date = self.request.query_params.get("date")

        if query_date:
            return MealRecord.objects.filter(
                user=user,
                meal_time__date=query_date
            ).order_by("-meal_time")

        return MealRecord.objects.filter(
            user=user
        ).order_by("-meal_time")


# ------------------------------------------
# 3) 상세 조회
# ------------------------------------------
class MealRecordDetailAPIView(generics.RetrieveAPIView):
    serializer_class = MealRecordDetailSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return MealRecord.objects.filter(user=self.request.user)


# ------------------------------------------
# 4) 주간 요약
# ------------------------------------------
class WeeklyStatsAPIView(generics.GenericAPIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = now().date()
        start = today - timedelta(days=6)

        qs = MealRecord.objects.filter(
            user=request.user,
            meal_time__date__range=[start, today]
        ).values("foods__kcal")

        data = MealRecord.objects.filter(
            user=request.user,
            meal_time__date__range=[start, today]
        ).aggregate(
            total_kcal=Sum("foods__kcal"),
            total_carb=Sum("foods__carb"),
            total_protein=Sum("foods__protein"),
            total_fat=Sum("foods__fat"),
            total_sugar=Sum("foods__sugar"),
        )

        return Response({
            "start_date": str(start),
            "end_date": str(today),
            "totals": data,
        })


# ------------------------------------------
# 5) 월간 요약
# ------------------------------------------
class MonthlyStatsAPIView(generics.GenericAPIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = now().date()
        start = today.replace(day=1)

        data = MealRecord.objects.filter(
            user=request.user,
            meal_time__date__range=[start, today]
        ).aggregate(
            total_kcal=Sum("foods__kcal"),
            total_carb=Sum("foods__carb"),
            total_protein=Sum("foods__protein"),
            total_fat=Sum("foods__fat"),
            total_sugar=Sum("foods__sugar"),
        )

        return Response({
            "month": today.strftime("%Y-%m"),
            "totals": data,
        })
