# backend/users/urls.py
from django.urls import path
from .views import KakaoLoginView

urlpatterns = [
    path('kakao/callback/', KakaoLoginView.as_view(), name='kakao_login'),
]
