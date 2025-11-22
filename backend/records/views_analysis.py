# records/views_analysis.py

from datetime import datetime, timedelta
from rest_framework import generics, permissions
from rest_framework.response import Response
from django.utils.timezone import now

from .models import MealRecord, WeightRecord
from .serializers import (
    DailyStatSerializer,
    WeeklyAnalysisSerializer,
    WeeklyWeightSerializer
)


# 🔧 공통: 주간 시작/끝 계산
def get_week_range(date):
    weekday = date.weekday()   # Monday=0
    start = date - timedelta(days=weekday)
    end = start + timedelta(days=6)
    return start, end


# -----------------------------------------
# 1) 날짜별 칼로리 통계
# -----------------------------------------
class DailyStatAPIView(generics.GenericAPIView):
    serializer_class = DailyStatSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        date_str = request.GET.get("date")
        if not date_str:
            return Response({"error": "date 파라미터 필요"}, status=400)

        date = datetime.strptime(date_str, "%Y-%m-%d").date()

        records = MealRecord.objects.filter(
            user=request.user,
            meal_time__date=date
        )

        total = sum(r.total_calories for r in records)

        return Response({
            "date": date,
            "total_calories": total,
        })
        
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

        daily_map = {
            (week_start + timedelta(days=i)): 0 for i in range(7)
        }

        records = MealRecord.objects.filter(
            user=request.user,
            meal_time__date__range=(week_start, week_end)
        )

        for r in records:
            d = r.meal_time.date()
            daily_map[d] += r.total_calories

        return Response({
            "week_start": str(week_start),
            "week_end": str(week_end),
            "weekly_records": [
                {"date": str(d), "calories": cal}
                for d, cal in daily_map.items()
            ]
        })

class WeeklyWeightAPIView(generics.GenericAPIView):
    serializer_class = WeeklyWeightSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        date_str = request.GET.get("date")
        if date_str:
            base_date = datetime.strptime(date_str, "%Y-%m-%d").date()
        else:
            base_date = now().date()

        week_start, week_end = get_week_range(base_date)

        weights = WeightRecord.objects.filter(
            user=request.user,
            date__range=(week_start, week_end)
        ).order_by("date")

        return Response({
            "week_start": str(week_start),
            "week_end": str(week_end),
            "records": [
                {"date": str(w.date), "weight": w.weight}
                for w in weights
            ]
        })
