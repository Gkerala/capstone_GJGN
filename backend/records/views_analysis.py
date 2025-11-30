# backend/records/views_analysis.py
from datetime import datetime, timedelta
from rest_framework import generics, permissions
from rest_framework.response import Response
from django.utils.timezone import now
from django.db.models.functions import Cast
from django.db.models import DateField
from datetime import date
from .models import MealRecord, MealFood,WeightRecord
from .serializers import (
    DailyStatSerializer,
    WeeklyAnalysisSerializer,
    WeeklyWeightSerializer
)

# 🔧 주간 시작/끝 계산
def get_week_range(date):
    weekday = date.weekday()   # Monday=0
    start = date - timedelta(days=weekday)
    end = start + timedelta(days=6)
    return start, end


# -----------------------------------------
# 1) 날짜별 칼로리 통계 (MealFood 기반)
# -----------------------------------------
class DailyStatAPIView(generics.GenericAPIView):
    serializer_class = DailyStatSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        date_str = request.GET.get("date")

        if not date_str:
            return Response({"error": "date 파라미터 필요"}, status=400)

        try:
            date = datetime.strptime(date_str, "%Y-%m-%d").date()
        except ValueError:
            return Response({"error": "잘못된 날짜 형식 (YYYY-MM-DD)"}, status=400)

        # 1) 해당 날짜의 MealRecord 조회
        meal_records = MealRecord.objects.filter(
            user=request.user,
            meal_time__date=date
        )

        record_ids = meal_records.values_list("id", flat=True)

        # 2) MealFood 조회 후 kcal 합산
        meal_foods = MealFood.objects.filter(record_id__in=record_ids)

        total_kcal = sum(mf.kcal for mf in meal_foods)

        # 3) 응답
        return Response({
            "date": str(date),
            "total_calories": total_kcal,
        })


# -----------------------------------------
# 2) 주간 칼로리 분석 (MealFood 기반)
# -----------------------------------------
class WeeklyAnalysisAPIView(generics.GenericAPIView):
    serializer_class = WeeklyAnalysisSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        date_str = request.GET.get("date")
        if date_str:
            base_date = datetime.strptime(date_str, "%Y-%m-%d").date()
        else:
            base_date = now().date()

        week_start, week_end = get_week_range(base_date)

        # 날짜 + 유저 필터 정확하게 하려면 meal_time을 Date로 변환해서 필터링해야함
        records = (
            MealRecord.objects.annotate(
                meal_date=Cast('meal_time', DateField())
            )
            .filter(
                user=request.user,
                meal_date__range=(week_start, week_end)
            )
        )

        # 일자별 칼로리 합산용 딕셔너리 생성
        daily_map = {
            (week_start + timedelta(days=i)): 0
            for i in range(7)
        }

        # MealFood에서 kcal 합산
        for rec in records:
            total_kcal = sum(f.kcal for f in rec.foods.all())
            d = rec.meal_time.date()
            if d in daily_map:
                daily_map[d] += total_kcal

        print("\n================ [WeeklyAnalysis] ================")
        print(f"📅 요청 날짜: {base_date}")
        print(f"🔍 주 시작/끝: {week_start} ~ {week_end}")
        print(f"📦 조회된 MealRecord 개수: {records.count()}")
        print(f"📊 day별 칼로리 합계: {daily_map}")
        print("==================================================\n")

        return Response({
            "week_start": str(week_start),
            "week_end": str(week_end),
            "weekly_records": [
                {"date": str(d), "calories": cal}
                for d, cal in daily_map.items()
            ]
        })



# -----------------------------------------
# 3) 주간 체중 분석
# -----------------------------------------
class WeeklyWeightAPIView(generics.GenericAPIView):
    serializer_class = WeeklyWeightSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        date_str = request.GET.get("date")
        base_date = (
            datetime.strptime(date_str, "%Y-%m-%d").date()
            if date_str else now().date()
        )

        week_start, week_end = get_week_range(base_date)
        dates = [week_start + timedelta(days=i) for i in range(7)]

        # 🔥 날짜별 최신 체중만 가져오기
        weights = (
            WeightRecord.objects
            .filter(user=request.user, date__range=(week_start, week_end))
            .order_by("date", "-created_at")
        )

        latest = {}
        for w in weights:
            # 같은 날짜면 created_at이 가장 늦은 값이 먼저 들어오므로 override되지 않음
            if w.date not in latest:
                latest[w.date] = w.weight

        # 날짜 순서대로 채움
        records = [
            {"date": str(d), "weight": latest.get(d)}
            for d in dates
        ]

        return Response({
            "week_start": str(week_start),
            "week_end": str(week_end),
            "records": records
        })





class TodayStatAPIView(generics.GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        today = date.today()

        records = MealRecord.objects.filter(
            user=request.user,
            meal_time__date=today
        ).order_by("-meal_time")

        total_kcal = sum(r.total_calories for r in records)
        count = records.count()

        # 최근 2개 음식명
        recent_foods = []
        for r in records[:2]:
            if r.memo:
                recent_foods.append(r.memo)
            else:
                recent_foods.append("식사 기록")

        return Response({
            "date": str(today),
            "total_kcal": total_kcal,
            "count": count,
            "recent": recent_foods
        })