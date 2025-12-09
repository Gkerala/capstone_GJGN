# backend/records/views.py
from datetime import timedelta, datetime, time
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
)


# ------------------------------------------
# 🔥 KST 기준 오늘 날짜 범위(UTC 변환)
# ------------------------------------------
def get_today_utc_range():
    kst_now = timezone.localtime(timezone.now())   # 항상 KST로 변환
    today_kst = kst_now.date()

    start_kst = timezone.make_aware(datetime.combine(today_kst, time.min))
    end_kst = timezone.make_aware(datetime.combine(today_kst, time.max))

    # 🔥 UTC 변환
    start_utc = start_kst.astimezone(timezone.utc)
    end_utc = end_kst.astimezone(timezone.utc)

    return start_utc, end_utc


# ------------------------------------------
# 1) 식단 기록 저장 API (UTC로 저장)
# ------------------------------------------
class MealRecordCreateAPIView(generics.CreateAPIView):
    serializer_class = MealRecordCreateSerializer

    def perform_create(self, serializer):
        obj = serializer.save()
        obj.meal_time = timezone.now().astimezone(timezone.utc)
        obj.save()


# ------------------------------------------
# 2) 날짜별 리스트
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
        today = timezone.localtime().date()
        start = today - timedelta(days=6)

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
        today = timezone.localtime().date()
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


# ------------------------------------------
# 6) 몸무게 저장 (UTC로 저장)
# ------------------------------------------
class WeightRecordCreateAPIView(generics.CreateAPIView):
    serializer_class = WeightRecordCreateSerializer
    permission_classes = [permissions.IsAuthenticated]

    def perform_create(self, serializer):
        obj = serializer.save()
        obj.created_at = timezone.now().astimezone(timezone.utc)
        obj.save()


class WeightRecordListAPIView(generics.ListAPIView):
    serializer_class = WeightRecordSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return WeightRecord.objects.filter(user=self.request.user).order_by("-date")


# ------------------------------------------
# 7) 오늘 식단 조회 (UTC 저장 → KST 기준 날짜 검색)
# ------------------------------------------
@api_view(["GET"])
def get_today_meals(request):
    user = request.user

    start_utc, end_utc = get_today_utc_range()

    queryset = MealRecord.objects.filter(
        user=user,
        meal_time__range=(start_utc, end_utc)
    ).order_by("-meal_time")

    # 정리된 응답
    result = {"breakfast": [], "lunch": [], "dinner": []}

    for record in queryset:
        foods = [
            {
                "id": f.id,
                "name": f.food_name,
                "kcal": f.kcal,
                "carbs": f.carb,
                "protein": f.protein,
                "fat": f.fat,
            }
            for f in record.foods.all()
        ]

        result[record.meal_type].extend(foods)

    # 🔥 요청 결과 JSON을 콘솔에 출력
    import json
    print("🔵 [get_today_meals RESPONSE]:\n",
          json.dumps(result, ensure_ascii=False, indent=2))

    return Response(result)



# ------------------------------------------
# 8) 식단 추가 (UTC로 저장)
# ------------------------------------------
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

    now_kst = timezone.localtime()
    meal_time_utc = now_kst.astimezone(timezone.utc)

    # 오늘의 해당 meal_type 레코드
    record, _ = MealRecord.objects.get_or_create(
        user=user,
        meal_type=meal_type,
        meal_time__date=now_kst.date(),
        defaults={"meal_type": meal_type, "meal_time": meal_time_utc}
    )

    # 음식 추가
    MealFood.objects.create(
        record=record,
        food_name=food_name,
        amount=1,
        kcal=kcal,
        carb=carb,
        protein=protein,
        fat=fat,
        sugar=0,
    )

    return Response({"message": "ok"}, status=201)


# ------------------------------------------
# 9) 식단 항목 삭제
# ------------------------------------------
@api_view(["DELETE"])
@permission_classes([IsAuthenticated])
def delete_meal_item(request, id):
    try:
        food = MealFood.objects.get(id=id, record__user=request.user)
    except MealFood.DoesNotExist:
        return Response({"error": "not found"}, status=404)

    food.delete()
    return Response(status=204)


# ------------------------------------------
# 10) 오늘 몸무게 조회 (KST 기준 조회)
# ------------------------------------------
@api_view(["GET"])
def get_today_weight(request):
    user = request.user
    start_utc, end_utc = get_today_utc_range()

    record = WeightRecord.objects.filter(
        user=user,
        created_at__range=(start_utc, end_utc)
    ).order_by("-created_at").first()

    if not record:
        return Response({"weight": None})

    return Response({"weight": record.weight})
