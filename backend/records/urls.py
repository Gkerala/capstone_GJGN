from django.urls import path
from .views import (
    MealRecordCreateAPIView,
    MealRecordListAPIView,
    MealRecordDetailAPIView,
    WeeklyStatsAPIView,
    MonthlyStatsAPIView,
    WeightRecordCreateAPIView, 
    WeightRecordListAPIView
)

from .views_main import MainSummaryAPIView

from .views_analysis import (
    DailyStatAPIView,
    WeeklyAnalysisAPIView,
    WeeklyWeightAPIView,
    TodayStatAPIView,
)

urlpatterns = [
    # ------------------------------------------------------
    # 📌 식단 기록
    # ------------------------------------------------------
    path("", MealRecordListAPIView.as_view()),
    path("create/", MealRecordCreateAPIView.as_view()),
    path("<int:pk>/", MealRecordDetailAPIView.as_view()),

    # ------------------------------------------------------
    # 📌 칼로리 통계 (Weekly, Monthly) — 기존 기능
    # ------------------------------------------------------
    path("stats/weekly/", WeeklyStatsAPIView.as_view()),
    path("stats/monthly/", MonthlyStatsAPIView.as_view()),

    # ------------------------------------------------------
    # 📌 체중 기록
    # ------------------------------------------------------
    path("weights/create/", WeightRecordCreateAPIView.as_view()),
    path("weights/", WeightRecordListAPIView.as_view()),

    # ------------------------------------------------------
    # 📊 분석 페이지용 (AnalysisActivity)
    # ------------------------------------------------------
    path("stats/daily/", DailyStatAPIView.as_view()),

    # 🔥 주간 칼로리 분석
    path("analysis/weekly/", WeeklyAnalysisAPIView.as_view()),

    # 🔥 주간 체중 분석 (AnalysisActivity)
    path("analysis/weights/weekly/", WeeklyWeightAPIView.as_view()),
    path("today/stat/", TodayStatAPIView.as_view()),
    
    path("main/summary/", MainSummaryAPIView.as_view()),

]
