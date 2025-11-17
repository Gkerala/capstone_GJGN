# goals/serializers.py
from rest_framework import serializers
from .models import NutritionGoal, WeightRecord


# ---------------------------------------------------------
# 📌 1) NutritionGoal 기본 Serializer
# ---------------------------------------------------------
class NutritionGoalSerializer(serializers.ModelSerializer):
    class Meta:
        model = NutritionGoal
        fields = "__all__"
        read_only_fields = ("user", "bmr", "activity_level", "updated_at")


# ---------------------------------------------------------
# 📌 2) NutritionGoal 수동 수정용 Serializer
# ---------------------------------------------------------
class GoalUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = NutritionGoal
        fields = ("calorie", "protein", "carbs", "fat")


# ---------------------------------------------------------
# 📌 3) 체중 기록 생성용 Serializer
# ---------------------------------------------------------
class WeightRecordCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = WeightRecord
        fields = ["weight"]
