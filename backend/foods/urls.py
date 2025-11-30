# backend/foods/urls.py
from django.urls import path
from .views import (
    FoodListView,
    FoodCreateView,
    FoodAnalyzeView,
    FoodSearchView,
    FoodAllListView,
)
from .views_foodnames import FoodNameListAPIView
from .views_nutrition import NutritionAPIView

urlpatterns = [
    path("", FoodListView.as_view(), name="food-list"),
    path("create/", FoodCreateView.as_view(), name="food-create"),
    path("analyze/", FoodAnalyzeView.as_view(), name="food-analyze"),
    path("nutrition/", NutritionAPIView.as_view(), name="nutrition"),
    path("search/", FoodSearchView.as_view(), name="food-search"),
    path("all/", FoodAllListView.as_view(), name="food-all"),
    path("api/foods/yolo-names/", FoodNameListAPIView.as_view()),

]
