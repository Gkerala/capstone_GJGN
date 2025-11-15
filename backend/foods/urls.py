from django.urls import path
from .views import (
    FoodListView,
    FoodCreateView,
    FoodAnalyzeView,
)
from .views_nutrition import NutritionAPIView

urlpatterns = [
    path("", FoodListView.as_view(), name="food-list"),
    path("create/", FoodCreateView.as_view(), name="food-create"),
    path("analyze/", FoodAnalyzeView.as_view(), name="food-analyze"),
    path("nutrition/", NutritionAPIView.as_view()),
]
