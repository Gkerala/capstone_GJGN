# backend/records/views.py
from datetime import timedelta
from datetime import datetime, time
from django.db.models import Sum
from django.utils.timezone import now

from rest_framework import generics, permissions
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

from rest_framework.decorators import api_view, permission_classes
from django.utils import timezone

from .models import MealRecord, WeightRecord, MealFood
from .serializers import (
    MealRecordCreateSerializer,
    WeightRecordSerializer,
    WeightRecordCreateSerializer,
    MealRecordListSerializer,
    MealRecordDetailSerializer,
    MealRecordSerializer,
)

def get_today_utc_range():
    # 🔥 localdate() 대신 timezone.now().date() 사용
    today = timezone.now().date()  # 이미 aware datetime → safe

    start_kst = datetime.combine(today, time.min)
    end_kst = datetime.combine(today, time.max)

    start_kst = timezone.make_aware(start_kst, timezone.get_current_timezone())
    end_kst = timezone.make_aware(end_kst, timezone.get_current_timezone())

    start_utc = start_kst.astimezone(timezone.utc)
    end_utc = end_kst.astimezone(timezone.utc)

    return start_utc, end_utc


# ------------------------------------------
# 1) 식단 기록 저장 API
# ------------------------------------------
class MealRecordCreateAPIView(generics.CreateAPIView):
    serializer_class = MealRecordCreateSerializer

    def perform_create(self, serializer):
        serializer.save()


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

class WeightRecordCreateAPIView(generics.CreateAPIView):
    serializer_class = WeightRecordCreateSerializer
    permission_classes = [permissions.IsAuthenticated]

    def perform_create(self, serializer):
        serializer.save()  # user는 serializer에서 자동 처리됨


class WeightRecordListAPIView(generics.ListAPIView):
    serializer_class = WeightRecordSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return WeightRecord.objects.filter(user=self.request.user).order_by("-date")
    
@api_view(["GET"])
def get_today_meals(request):
    user = request.user

    start_utc, end_utc = get_today_utc_range()

    queryset = MealRecord.objects.filter(
        user=user,
        meal_time__range=(start_utc, end_utc)
    ).order_by("-meal_time")

    print("🔥 [meal/today] UTC RANGE:", start_utc, "~", end_utc)
    print("🔥 [meal/today] RECORDS COUNT:", queryset.count())

    # meal_type에 따라 분류
    result = {"breakfast": [], "lunch": [], "dinner": []}

    for record in queryset:
        foods = [
            {
                "id": food.id,
                "name": food.food_name,
                "kcal": food.kcal,
                "carbs": food.carb,
                "protein": food.protein,
                "fat": food.fat,
            }
            for food in record.foods.all()
        ]
        result[record.meal_type].extend(foods)

    print("🔥 [meal/today] FINAL RESPONSE:", result)

    return Response(result)



@api_view(["POST"])
@permission_classes([IsAuthenticated])
def add_meal(request):
    user = request.user

    meal_type = request.data.get("mealType")
    food_name = request.data.get("name")

    kcal = request.data.get("kcal", 0)
    carb = request.data.get("carbs", 0)
    protein = request.data.get("protein", 0)
    fat = request.data.get("fat", 0)

    if not meal_type or not food_name:
        return Response({"error": "missing fields"}, status=400)

    today = timezone.now().date()

    # 오늘 해당 식사(meal_type)의 레코드가 이미 있는지 확인
    record, _ = MealRecord.objects.get_or_create(
        user=user,
        meal_type=meal_type,
        meal_time__date=today,
        defaults={"meal_type": meal_type}
    )

    # MealFood 추가
    MealFood.objects.create(
        record=record,
        food_name=food_name,
        amount=1,     # 기본값 1
        kcal=kcal,
        carb=carb,
        protein=protein,
        fat=fat,
        sugar=0
    )

    return Response({"message": "ok"}, status=201)

@api_view(["DELETE"])
@permission_classes([IsAuthenticated])
def delete_meal_item(request, id):
    try:
        food = MealFood.objects.get(id=id, record__user=request.user)
    except MealFood.DoesNotExist:
        return Response({"error": "not found"}, status=404)

    food.delete()
    return Response(status=204)

@api_view(["GET"])
def get_today_weight(request):
    user = request.user

    start_utc, end_utc = get_today_utc_range()

    record = WeightRecord.objects.filter(
        user=user,
        created_at__range=(start_utc, end_utc)
    ).order_by("-created_at").first()

    print("🔥 [weight/today] UTC RANGE:", start_utc, "~", end_utc)
    print("🔥 [weight/today] FOUND:", record)

    if not record:
        return Response({"weight": None})

    return Response({"weight": record.weight})
