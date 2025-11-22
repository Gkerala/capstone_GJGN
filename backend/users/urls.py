from django.urls import path
from users.views import UserDetailView, UserFullProfileUpdateView, UserDeleteView
from users.views_auth import KakaoLoginView

urlpatterns = [
    path("login/kakao/", KakaoLoginView.as_view()),

    path("me/", UserDetailView.as_view()),

    path("me/profile/", UserFullProfileUpdateView.as_view()),

    path("delete/", UserDeleteView.as_view()),
]
