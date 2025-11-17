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


# ----------------------------
# 📌 여기 추가된 부분 (Detail)
# ----------------------------
class MealRecordDetailSerializer(serializers.ModelSerializer):
    foods = MealFoodSerializer(many=True)
    total_calories = serializers.SerializerMethodField()
    total_carbs = serializers.SerializerMethodField()
    total_protein = serializers.SerializerMethodField()
    total_fat = serializers.SerializerMethodField()

    class Meta:
        model = MealRecord
        fields = [
            "id",
            "meal_time",
            "memo",
            "image",
            "foods",
            "total_calories",
            "total_carbs",
            "total_protein",
            "total_fat",
            "created_at"
        ]

    def get_total_calories(self, obj):
        return sum(f.calories for f in obj.foods.all())

    def get_total_carbs(self, obj):
        return sum(f.carbs for f in obj.foods.all())

    def get_total_protein(self, obj):
        return sum(f.protein for f in obj.foods.all())

    def get_total_fat(self, obj):
        return sum(f.fat for f in obj.foods.all())


# ----------------------------
# 📌 (옵션) List 전용 Serializer
# ----------------------------
class MealRecordListSerializer(serializers.ModelSerializer):
    class Meta:
        model = MealRecord
        fields = [
            "id",
            "meal_time",
            "created_at",
            "image"
        ]
