from datetime import datetime, timedelta
from rest_framework import generics, permissions
from rest_framework.response import Response
from django.utils.timezone import now
from datetime import date
from .models import MealRecord, WeightRecord
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
            "date": str(date),
            "total_calories": total,
        })


# -----------------------------------------
# 2) 주간 칼로리 분석
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

        # 날짜 → 칼로리 총합
        daily_map = {
            (week_start + timedelta(days=i)): 0
            for i in range(7)
        }

        records = MealRecord.objects.filter(
            user=request.user,
            meal_time__date__range=(week_start, week_end)
        )

        # 날짜별 칼로리 합산
        for r in records:
            d = r.meal_time.date()
            if d in daily_map:
                daily_map[d] += r.total_calories

        # 날짜를 문자열로 변환 후 전달
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
        if date_str:
            base_date = datetime.strptime(date_str, "%Y-%m-%d").date()
        else:
            base_date = now().date()

        # 주 시작(월요일) ~ 주 종료(일요일)
        week_start, week_end = get_week_range(base_date)

        # 7일 기본 틀 생성 (월~일)
        daily_weights = {
            (week_start + timedelta(days=i)): None for i in range(7)
        }

        # DB에서 실제 몸무게 데이터 가져오기
        weights = WeightRecord.objects.filter(
            user=request.user,
            date__range=(week_start, week_end)
        )

        # 실제 기록이 있는 날짜는 대체하기
        for w in weights:
            daily_weights[w.date] = w.weight

        # 응답 구조 통일 (항상 7개)
        return Response({
            "week_start": str(week_start),
            "week_end": str(week_end),
            "records": [
                {
                    "date": str(d),
                    "weight": daily_weights[d]
                }
                for d in sorted(daily_weights.keys())
            ]
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