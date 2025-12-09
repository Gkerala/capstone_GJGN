# backend/records/urls.py
from django.urls import path
from .views import (
    MealRecordCreateAPIView,
    MealRecordListAPIView,
    MealRecordDetailAPIView,
    WeeklyStatsAPIView,
    MonthlyStatsAPIView,
    WeightRecordCreateAPIView, 
    WeightRecordListAPIView,
    
    get_today_meals,
    add_meal,
    delete_meal_item,
    get_today_weight,
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
    
    path("meal/today/", get_today_meals),
    path("meal/add/", add_meal),
    path("meal/delete/<int:id>/", delete_meal_item),
    path("weight/today/", get_today_weight),


]
