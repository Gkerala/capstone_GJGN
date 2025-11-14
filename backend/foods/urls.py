from django.urls import path
from .views import (
    FoodListView,
    FoodCreateView,
    FoodAnalyzeView
)

urlpatterns = [
    path("", FoodListView.as_view(), name="food-list"),
    path("create/", FoodCreateView.as_view(), name="food-create"),
    path("analyze/", FoodAnalyzeView.as_view(), name="food-analyze"),
]
