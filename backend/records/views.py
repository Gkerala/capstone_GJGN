from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from rest_framework import generics, status
from rest_framework.parsers import MultiPartParser, FormParser

from datetime import date, timedelta
from django.db.models import Sum
from django.shortcuts import get_object_or_404
from django.utils.timezone import now

from foods.models import Food
from .models import MealRecord, MealFood
from .serializers import (
    MealRecordCreateSerializer,
    MealRecordDetailSerializer,
    MealRecordListSerializer,
    MealRecordSerializer,
    MealFoodSerializer,
)

import json
import requests


# -------------------------------------------------------------------
# 📌 [1] 기존 MealRecord 생성 API 그대로 유지
# -------------------------------------------------------------------
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


# -------------------------------------------------------------------
# 📌 [2] 기존 MealRecord 목록 조회 API 그대로 유지
# -------------------------------------------------------------------
class MealRecordListAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        records = MealRecord.objects.filter(user=user).order_by("-meal_time")
        serializer = MealRecordListSerializer(records, many=True)
        return Response(serializer.data, status=200)


# -------------------------------------------------------------------
# 📌 [3] 기존 상세조회 + 삭제 API 그대로 유지
# -------------------------------------------------------------------
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


# -------------------------------------------------------------------
# 📌 [4] 기존 수정 API 그대로 유지
# -------------------------------------------------------------------
class MealRecordUpdateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request, pk):
        record = get_object_or_404(MealRecord, id=pk, user=request.user)

        meal_time = request.data.get("meal_time")
        memo = request.data.get("memo")

        if meal_time:
            record.meal_time = meal_time
        if memo is not None:
            record.memo = memo

        record.save()

        foods_raw = request.data.get("foods")
        if foods_raw:
            try:
                foods_data = json.loads(foods_raw) if isinstance(foods_raw, str) else foods_raw
            except:
                return Response({"detail": "foods must be valid JSON list"}, status=400)

            # 기존 삭제 후 재생성
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


# -------------------------------------------------------------------
# 📌 [5] 통계 계산 로직 (공용)
# -------------------------------------------------------------------
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


# -------------------------------------------------------------------
# 📌 5-1) 오늘 통계
# -------------------------------------------------------------------
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


# -------------------------------------------------------------------
# 📌 5-2) 이번주 통계
# -------------------------------------------------------------------
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


# -------------------------------------------------------------------
# 📌 5-3) 이번달 통계
# -------------------------------------------------------------------
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


# -------------------------------------------------------------------
# 📌 [6] 이미지 분석 (기존 그대로)
# -------------------------------------------------------------------
class AnalyzeFoodImageView(APIView):
    parser_classes = [MultiPartParser, FormParser]
    permission_classes = [IsAuthenticated]

    def post(self, request):

        if "image" not in request.FILES:
            return Response({"error": "image file is required"}, status=400)

        image = request.FILES["image"]

        ai_url = "http://localhost:8001/predict"
        files = {"image": image.read()}

        resp = requests.post(ai_url, files=files)
        if resp.status_code != 200:
            return Response({"error": "AI server error"}, status=500)

        ai_result = resp.json()

        meal_record = MealRecord.objects.create(
            user=request.user,
            meal_time=request.data.get("meal_time", "unknown"),
            memo=request.data.get("memo", ""),
            image=image
        )

        for item in ai_result.get("foods", []):
            name = item["name"]
            amount = item["amount"]

            try:
                food = Food.objects.get(name=name)
            except Food.DoesNotExist:
                continue

            MealFood.objects.create(
                record=meal_record,
                food=food,
                amount=amount,
                calories=food.calories * (amount / 100),
                carbs=food.carbs * (amount / 100),
                protein=food.protein * (amount / 100),
                fat=food.fat * (amount / 100),
            )

        return Response(MealRecordSerializer(meal_record).data, status=201)



# -------------------------------------------------------------------
# 📌 [7] URL에서 요구한 View 추가 (누락 해결)
# -------------------------------------------------------------------

# List + Create (URL에서 불러서 오류 발생했음)
class MealRecordListCreateView(generics.ListCreateAPIView):
    serializer_class = MealRecordSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return MealRecord.objects.filter(user=self.request.user).order_by("-created_at")


# DetailView (URL에서 요구)
class MealRecordDetailView(generics.RetrieveAPIView):
    serializer_class = MealRecordSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return MealRecord.objects.filter(user=self.request.user)


# 하루 요약
class DailySummaryView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = now().date()

        foods = MealFood.objects.filter(
            record__user=request.user,
            record__created_at__date=today
        )

        summary = foods.aggregate(
            calories=Sum("calories"),
            carbs=Sum("carbs"),
            protein=Sum("protein"),
            fat=Sum("fat")
        )

        return Response(summary, status=200)


# 하루 모든 기록 (일별 식단 리스트)
class DailyStatsView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = now().date()
        records = MealRecord.objects.filter(
            user=request.user,
            created_at__date=today
        )

        return Response({
            "count": records.count(),
            "records": MealRecordSerializer(records, many=True).data
        })


# 하루 분석 (식사별 분석 포함)
class DailyAnalysisView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = now().date()
        records = MealRecord.objects.filter(
            user=request.user,
            created_at__date=today
        )

        analysis = {}

        for rec in records:
            key = rec.meal_time
            foods = MealFood.objects.filter(record=rec)

            if key not in analysis:
                analysis[key] = {
                    "calories": 0,
                    "carbs": 0,
                    "protein": 0,
                    "fat": 0,
                    "items": []
                }

            for f in foods:
                analysis[key]["calories"] += f.calories
                analysis[key]["carbs"] += f.carbs
                analysis[key]["protein"] += f.protein
                analysis[key]["fat"] += f.fat
                analysis[key]["items"].append(MealFoodSerializer(f).data)

        return Response(analysis, status=200)

# -------------------------------------------------------------------
# 📌 주간 분석
# -------------------------------------------------------------------
class WeeklyAnalysisView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = date.today()
        start_week = today - timedelta(days=today.weekday())
        end_week = start_week + timedelta(days=6)

        records = MealRecord.objects.filter(
            user=request.user,
            meal_time__date__range=[start_week, end_week]
        )

        analysis = {}

        for rec in records:
            key = rec.meal_time

            foods = MealFood.objects.filter(record=rec)

            if key not in analysis:
                analysis[key] = {
                    "calories": 0,
                    "carbs": 0,
                    "protein": 0,
                    "fat": 0,
                    "items": []
                }

            for f in foods:
                analysis[key]["calories"] += f.calories
                analysis[key]["carbs"] += f.carbs
                analysis[key]["protein"] += f.protein
                analysis[key]["fat"] += f.fat
                analysis[key]["items"].append(MealFoodSerializer(f).data)

        return Response({
            "start_date": str(start_week),
            "end_date": str(end_week),
            "analysis": analysis
        })


# -------------------------------------------------------------------
# 📌 월간 분석
# -------------------------------------------------------------------
class MonthlyAnalysisView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = date.today()
        start_month = today.replace(day=1)
        next_month = (start_month.replace(month=(start_month.month % 12) + 1, day=1))
        end_month = next_month - timedelta(days=1)

        records = MealRecord.objects.filter(
            user=request.user,
            meal_time__date__range=[start_month, end_month]
        )

        analysis = {}

        for rec in records:
            key = rec.meal_time

            foods = MealFood.objects.filter(record=rec)

            if key not in analysis:
                analysis[key] = {
                    "calories": 0,
                    "carbs": 0,
                    "protein": 0,
                    "fat": 0,
                    "items": []
                }

            for f in foods:
                analysis[key]["calories"] += f.calories
                analysis[key]["carbs"] += f.carbs
                analysis[key]["protein"] += f.protein
                analysis[key]["fat"] += f.fat
                analysis[key]["items"].append(MealFoodSerializer(f).data)

        return Response({
            "start_date": str(start_month),
            "end_date": str(end_month),
            "analysis": analysis
        })

# -------------------------------------------------------------------
# 📌 주간 매크로 합계 (칼로리/탄수/단백질/지방)
# -------------------------------------------------------------------
class WeeklyMacroStatAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        today = date.today()
        start_week = today - timedelta(days=today.weekday())
        end_week = start_week + timedelta(days=6)

        foods = MealFood.objects.filter(
            record__user=request.user,
            record__meal_time__date__range=[start_week, end_week]
        )

        result = foods.aggregate(
            calories=Sum("calories"),
            carbs=Sum("carbs"),
            protein=Sum("protein"),
            fat=Sum("fat"),
        )

        return Response({
            "start_date": str(start_week),
            "end_date": str(end_week),
            "total_calories": result["calories"] or 0,
            "carbs": result["carbs"] or 0,
            "protein": result["protein"] or 0,
            "fat": result["fat"] or 0,
        })
