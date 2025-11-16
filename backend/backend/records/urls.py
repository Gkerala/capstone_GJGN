from django.urls import path
from .views import AnalyzeFoodImageView,MealRecordListCreateView, MealRecordDetailView, DailySummaryView, DailyStatsView, DailyAnalysisView
from .views import WeeklyAnalysisView, MonthlyAnalysisView
from .views_ai import AIFoodRecordCreateView
from .views import (
    TodayStatAPIView,
    WeeklyStatAPIView,
    WeeklyMacroStatAPIView,
    MonthlyStatAPIView,
)

urlpatterns = [
    path("", MealRecordListCreateView.as_view(), name="record_list_create"),
    path("<int:record_id>/", MealRecordDetailView.as_view(), name="record_detail"),
    path("summary/", DailySummaryView.as_view()),
    path("ai/", AIFoodRecordCreateView.as_view()),
    path("stats/daily/", DailyStatsView.as_view(), name="daily_stats"),
    path("analyze/", AnalyzeFoodImageView.as_view(), name="analyze_food"),
    path("analysis/daily/", DailyAnalysisView.as_view()),
    path("analysis/weekly/", WeeklyAnalysisView.as_view()),
    path("analysis/monthly/", MonthlyAnalysisView.as_view()),
    path("today/stat/", TodayStatAPIView.as_view(), name="today-stat"),
    path("weekly/", WeeklyStatAPIView.as_view()),
    path("week/stat/", WeeklyStatAPIView.as_view(), name="week-stat"),
    path("week/macro/", WeeklyMacroStatAPIView.as_view()),
    path("month/stat/", MonthlyStatAPIView.as_view()),
]
