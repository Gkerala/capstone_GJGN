from django.urls import path
from users.views import UserDetailView
from users.views_goal import UserGoalUpdateAPIView, UserGoalRetrieveAPIView
from users.views_auth import UserGoalUpdateView

urlpatterns = [
    path("me/", UserDetailView.as_view(), name="user-detail"),
    path("me/goal/", UserGoalUpdateView.as_view()),
    path("me/goal/", UserGoalUpdateAPIView.as_view(), name="user-goal-update"),
    path("me/goal/", UserGoalRetrieveAPIView.as_view(), name="goal-get"),
    path("me/goal/update/", UserGoalUpdateAPIView.as_view(), name="goal-update"),
]
