# goals/urls.py
from django.urls import path
from .views import (
    UserGoalRetrieveAPIView,
    UserGoalUpdateAPIView,
    AutoGoalGenerateAPIView,
)

urlpatterns = [
    path("me/", UserGoalRetrieveAPIView.as_view()),
    path("update/", UserGoalUpdateAPIView.as_view()),
    path("auto-generate/", AutoGoalGenerateAPIView.as_view()),
]

