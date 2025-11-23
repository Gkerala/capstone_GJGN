from rest_framework import serializers
from .models import UserGoal, WeightRecord

class UserGoalSerializer(serializers.ModelSerializer):

    class Meta:
        model = UserGoal
        fields = [
            "goal_type",
            "goal_weight",
            "activity_level",

            # 실제 계산된 목표값
            "kcal",
            "carbs",
            "protein",
            "fat",
            "sugar",
        ]



class UserGoalUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserGoal
        fields = ["goal_type", "goal_weight", "activity_level"]

    def update(self, instance, validated_data):
        for field, value in validated_data.items():
            setattr(instance, field, value)
        instance.save()
        return instance


class WeightRecordCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = WeightRecord
        fields = ["weight"]
