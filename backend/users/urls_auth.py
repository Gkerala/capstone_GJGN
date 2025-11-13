from django.urls import path
from .views_auth import KakaoLoginView, JWTTestView

urlpatterns = [
    path("login/kakao/", KakaoLoginView.as_view(), name="kakao_login"),
    path("jwt/test/", JWTTestView.as_view(), name="jwt_test"),
]
