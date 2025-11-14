from django.urls import path
from .views import (
    AutoGoalGenerateAPIView,
    GoalRetrieveAPIView,
    GoalUpdateAPIView,
    WeeklyGoalStatAPIView,
    MonthlyGoalStatAPIView,
)


urlpatterns = [
    path("auto/", AutoGoalGenerateAPIView.as_view()),
    path("get/", GoalRetrieveAPIView.as_view()),
    path("update/", GoalUpdateAPIView.as_view()),
    path("weekly/", WeeklyGoalStatAPIView.as_view()),
    path("monthly/", MonthlyGoalStatAPIView.as_view()),
]