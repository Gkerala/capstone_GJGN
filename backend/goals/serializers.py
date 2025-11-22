# goals/serializers.py
from rest_framework import serializers
from .models import NutritionGoal, WeightRecord


# NutritionGoal 전체 조회용
class NutritionGoalSerializer(serializers.ModelSerializer):
    class Meta:
        model = NutritionGoal
        fields = "__all__"
        read_only_fields = ("user", "bmr", "activity_level", "updated_at")


# NutritionGoal 수동 수정용
class GoalUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = NutritionGoal
        fields = ("calorie", "protein", "carbs", "fat")


# 체중 기록 생성
class WeightRecordCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = WeightRecord
        fields = ["weight"]
