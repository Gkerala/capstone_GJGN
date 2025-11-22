# backend/records/serializers.py
from rest_framework import serializers
from .models import MealRecord, MealFood


class MealFoodSerializer(serializers.ModelSerializer):
    class Meta:
        model = MealFood
        fields = [
            "id",
            "name",
            "amount",
            "kcal",
            "carb",
            "protein",
            "fat",
            "sugar",
        ]


class MealRecordCreateSerializer(serializers.ModelSerializer):
    foods = MealFoodSerializer(many=True)

    class Meta:
        model = MealRecord
        fields = ["meal_time", "memo", "image", "foods"]

    def create(self, validated_data):
        foods_data = validated_data.pop("foods")
        record = MealRecord.objects.create(**validated_data)

        for food in foods_data:
            MealFood.objects.create(record=record, **food)

        return record


class MealRecordListSerializer(serializers.ModelSerializer):
    class Meta:
        model = MealRecord
        fields = ["id", "meal_time", "image", "created_at"]


class MealRecordDetailSerializer(serializers.ModelSerializer):
    foods = MealFoodSerializer(many=True)

    total_kcal = serializers.SerializerMethodField()
    total_carb = serializers.SerializerMethodField()
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
            "total_kcal",
            "total_carb",
            "total_protein",
            "total_fat",
            "created_at",
        ]

    def get_total_kcal(self, obj):
        return sum(f.kcal for f in obj.foods.all())

    def get_total_carb(self, obj):
        return sum(f.carb for f in obj.foods.all())

    def get_total_protein(self, obj):
        return sum(f.protein for f in obj.foods.all())

    def get_total_fat(self, obj):
        return sum(f.fat for f in obj.foods.all())
