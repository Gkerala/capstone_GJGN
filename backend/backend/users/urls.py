from django.urls import path
from users.views import UserDetailView
from users.views_goal import UserGoalUpdateAPIView, UserGoalRetrieveAPIView
from users.views_auth import UserGoalUpdateView

urlpatterns = [
    # 사용자 기본 정보
    path("me/", UserDetailView.as_view(), name="user-detail"),

    # 목표 정보 조회
    path("me/goal/", UserGoalRetrieveAPIView.as_view(), name="goal-get"),

    # 목표 정보 업데이트 (PUT/PATCH)
    path("me/goal/update/", UserGoalUpdateAPIView.as_view(), name="goal-update"),
]
