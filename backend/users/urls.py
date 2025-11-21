# backend/users/urls.py
from django.urls import path
from users.views import UserDetailView
from users.views_goal import (
    UserGoalUpdateAPIView,
    UserGoalRetrieveAPIView,
)

urlpatterns = [
    # 🧍 1) 사용자 기본 정보 조회(GET) + 수정(PATCH)
    # GET  /api/users/me/
    # PATCH /api/users/me/
    path("me/", UserDetailView.as_view(), name="user-detail"),

    # 🎯 2) 목표 정보 조회 (UserGoal)
    # GET /api/users/me/goal/
    path("me/goal/", UserGoalRetrieveAPIView.as_view(), name="goal-get"),

    # 🎯 3) 목표 정보 수정 (UserGoal)
    # PATCH /api/users/me/goal/update/
    path("me/goal/update/", UserGoalUpdateAPIView.as_view(), name="goal-update"),
]
