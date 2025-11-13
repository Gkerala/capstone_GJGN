from django.urls import path
from . import views_auth

urlpatterns = [
    path('kakao/', views_auth.kakao_login, name='kakao_login'),
    path('kakao/callback/', views_auth.kakao_callback, name='kakao_callback'),
]
