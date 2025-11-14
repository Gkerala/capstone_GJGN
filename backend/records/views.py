from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from rest_framework import status

from datetime import date, timedelta
from django.db.models import Sum
from django.shortcuts import get_object_or_404

from foods.models import Food
from .models import MealRecord, MealFood
from .serializers import (
    MealRecordCreateSerializer,
    MealRecordDetailSerializer,
    MealRecordListSerializer
)

import json


# ---------------------------------------------------------
# 📌 1) MealRecord 생성
# ---------------------------------------------------------
class MealRecordCreateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user

        serializer = MealRecordCreateSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=400)

        foods_raw = request.data.get("foods")

        try:
            foods_data = json.loads(foods_raw) if isinstance(foods_raw, str) else foods_raw
        except:
            return Response({"detail": "foods must be valid JSON list"}, status=400)

        if not isinstance(foods_data, list):
            return Response({"detail": "foods must be a list"}, status=400)

        meal_record = serializer.save(user=user)

        for item in foods_data:
            food_name = item.get("name")
            amount = item.get("amount", 1)

            if not food_name:
                continue

            food_obj, _ = Food.objects.get_or_create(name=food_name)

            MealFood.objects.create(
                record=meal_record,
                food=food_obj,
                amount=amount,
                calories=food_obj.calories * amount,
                carbs=food_obj.carbs * amount,
                protein=food_obj.protein * amount,
                fat=food_obj.fat * amount,
            )

        return Response(
            {
                "message": "Meal record created successfully",
                "record_id": meal_record.id
            },
            status=201
        )


# ---------------------------------------------------------
# 📌 2) MealRecord 목록 조회
# ---------------------------------------------------------
class MealRecordListAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        records = MealRecord.objects.filter(user=user).order_by("-meal_time")
        serializer = MealRecordListSerializer(records, many=True)
        return Response(serializer.data, status=200)


# ---------------------------------------------------------
# 📌 3) MealRecord 상세 조회 + 삭제
# ---------------------------------------------------------
class MealRecordDetailAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request, pk):
        record = get_object_or_404(MealRecord, id=pk, user=request.user)
        serializer = MealRecordDetailSerializer(record)
        return Response(serializer.data, status=200)

    def delete(self, request, pk):
        record = get_object_or_404(MealRecord, id=pk, user=request.user)
        record.delete()
        return Response({"message": "Record deleted"}, status=204)


# ---------------------------------------------------------
# 📌 4) MealRecord 수정 (foods 포함)
# ---------------------------------------------------------
class MealRecordUpdateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request, pk):
        record = get_object_or_404(MealRecord, id=pk, user=request.user)

        # meal_time, memo 등 기본 필드 업데이트
        meal_time = request.data.get("meal_time")
        memo = request.data.get("memo")

        if meal_time:
            record.meal_time = meal_time

        if memo is not None:
            record.memo = memo

        record.save()

        # foods 업데이트
        foods_raw = request.data.get("foods")
        if foods_raw:
            try:
                foods_data = json.loads(foods_raw) if isinstance(foods_raw, str) else foods_raw
            except:
                return Response({"detail": "foods must be valid JSON list"}, status=400)

            # 기존 음식 삭제 후 재생성
            MealFood.objects.filter(record=record).delete()

            for item in foods_data:
                food_name = item.get("name")
                amount = item.get("amount", 1)

                if not food_name:
                    continue

                food_obj, _ = Food.objects.get_or_create(name=food_name)

                MealFood.objects.create(
                    record=record,
                    food=food_obj,
                    amount=amount,
                    calories=food_obj.calories * amount,
                    carbs=food_obj.carbs * amount,
                    protein=food_obj.protein * amount,
                    fat=food_obj.fat * amount,
                )

        return Response({"message": "Record updated"}, status=200)


# ---------------------------------------------------------
# 📌 5) 오늘 / 이번주 / 이번달 총 칼로리 & 탄단지
# ---------------------------------------------------------
def aggregate_meals(user, start_date, end_date):
    foods = MealFood.objects.filter(
        record__user=user,
        record__meal_time__date__range=[start_date, end_date]
    )

    return foods.aggregate(
        calories=Sum("calories"),
        carbs=Sum("carbs"),
        protein=Sum("protein"),
        fat=Sum("fat"),
    )


# 📌 5-1) 오늘 통계
class TodayStatAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = date.today()
        result = aggregate_meals(request.user, today, today)

        return Response({
            "date": str(today),
            "total_calories": result["calories"] or 0,
            "carbs": result["carbs"] or 0,
            "protein": result["protein"] or 0,
            "fat": result["fat"] or 0,
        })


# 📌 5-2) 이번주 통계
class WeeklyStatAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = date.today()
        start_week = today - timedelta(days=today.weekday())
        end_week = start_week + timedelta(days=6)

        result = aggregate_meals(request.user, start_week, end_week)

        return Response({
            "start_date": str(start_week),
            "end_date": str(end_week),
            "total_calories": result["calories"] or 0,
            "carbs": result["carbs"] or 0,
            "protein": result["protein"] or 0,
            "fat": result["fat"] or 0,
        })


# 📌 5-3) 이번달 통계
class MonthlyStatAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = date.today()
        start_month = today.replace(day=1)
        end_month = (start_month.replace(month=start_month.month % 12 + 1, day=1) - timedelta(days=1))

        result = aggregate_meals(request.user, start_month, end_month)

        return Response({
            "start_date": str(start_month),
            "end_date": str(end_month),
            "total_calories": result["calories"] or 0,
            "carbs": result["carbs"] or 0,
            "protein": result["protein"] or 0,
            "fat": result["fat"] or 0,
        })
