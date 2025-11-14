from rest_framework import serializers
from .models import NutritionGoal


class NutritionGoalSerializer(serializers.ModelSerializer):
    class Meta:
        model = NutritionGoal
        fields = "__all__"
        read_only_fields = ("user", "bmr", "activity_level", "updated_at")


class GoalUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = NutritionGoal
        fields = ("calorie", "protein", "carbs", "fat")
