from rest_framework import serializers
from .models import MealRecord, MealFood
from foods.serializers import FoodSerializer


class MealFoodCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = MealFood
        fields = ["food", "amount"]


class MealFoodSerializer(serializers.ModelSerializer):
    food = FoodSerializer()

    class Meta:
        model = MealFood
        fields = [
            "id",
            "food",
            "amount",
            "calories",
            "carbs",
            "protein",
            "fat"
        ]


class MealRecordCreateSerializer(serializers.ModelSerializer):
    foods = MealFoodCreateSerializer(many=True)

    class Meta:
        model = MealRecord
        fields = ["meal_time", "memo", "image", "foods"]


class MealRecordSerializer(serializers.ModelSerializer):
    foods = MealFoodSerializer(many=True)

    class Meta:
        model = MealRecord
        fields = [
            "id",
            "meal_time",
            "memo",
            "image",
            "foods",
            "created_at"
        ]
