# backend/foods/urls.py

from django.urls import path
from . import views

urlpatterns = [
    # 예시 URL — 나중에 실제 view 함수로 교체 가능
    path('', views.index, name='foods_index'),
]
