from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

from .services.stat_service import DailyStatService
from .services.weekly_service import WeeklyStatService


class TodayStatAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        data = DailyStatService.get_today_stats(request.user)
        return Response(data, status=200)

class WeeklyStatAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        data = WeeklyStatService.get_weekly_stats(request.user)
        return Response(data, status=200)