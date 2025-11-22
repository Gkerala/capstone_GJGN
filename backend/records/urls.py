# backend/records/urls.py
from django.urls import path
from .views import (
    MealRecordCreateAPIView,
    MealRecordListAPIView,
    MealRecordDetailAPIView,
    WeeklyStatsAPIView,
    MonthlyStatsAPIView,
)

urlpatterns = [
    path("", MealRecordListAPIView.as_view()),
    path("create/", MealRecordCreateAPIView.as_view()),
    path("<int:pk>/", MealRecordDetailAPIView.as_view()),
    path("stats/weekly/", WeeklyStatsAPIView.as_view()),
    path("stats/monthly/", MonthlyStatsAPIView.as_view()),
]
