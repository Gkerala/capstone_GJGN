from rest_framework import serializers
from .models import MealRecord, MealFood


# ----------------------------------------
# MealFood (기본 serializer)
# ----------------------------------------
class MealFoodSerializer(serializers.ModelSerializer):
    class Meta:
        model = MealFood
        fields = ["id", "food_name", "amount", "kcal", "carb", "protein", "fat", "sugar"]


# ----------------------------------------
# MealRecord 생성용 (앱에서 영양정보 포함해서 보냄)
# ----------------------------------------
class MealRecordCreateSerializer(serializers.ModelSerializer):
    foods = MealFoodSerializer(many=True)

    class Meta:
        model = MealRecord
        fields = ["meal_time", "memo", "image", "foods"]

    def create(self, validated_data):
        foods_data = validated_data.pop("foods")
        record = MealRecord.objects.create(**validated_data)

        for item in foods_data:
            MealFood.objects.create(record=record, **item)

        return record


# ----------------------------------------
# MealRecord 리스트용
# ----------------------------------------
class MealRecordListSerializer(serializers.ModelSerializer):
    class Meta:
        model = MealRecord
        fields = ["id", "meal_time", "image", "created_at"]


# ----------------------------------------
# MealRecord 상세보기용
# ----------------------------------------
class MealRecordDetailSerializer(serializers.ModelSerializer):
    foods = MealFoodSerializer(many=True)

    class Meta:
        model = MealRecord
        fields = [
            "id",
            "meal_time",
            "memo",
            "image",
            "foods",
            "created_at",
        ]


# ----------------------------------------
# 전체용 기본 serializer
# ----------------------------------------
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
            "created_at",
        ]
