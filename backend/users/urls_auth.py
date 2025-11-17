from django.urls import path
from users.views_auth import KakaoLoginView

urlpatterns = [
    path("login/kakao/", KakaoLoginView.as_view(), name="kakao-login"),
]
