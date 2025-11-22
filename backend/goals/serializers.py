# goals/serializers.py
from rest_framework import serializers
from .models import UserGoal, WeightRecord

class UserGoalSerializer(serializers.ModelSerializer):
    target_kcal = serializers.IntegerField(source='kcal')
    target_carb = serializers.IntegerField(source='carbs')
    target_protein = serializers.IntegerField(source='protein')
    target_fat = serializers.IntegerField(source='fat')

    class Meta:
        model = UserGoal
        fields = [
            "goal_type", "goal_weight", "activity_level",
            "target_kcal", "target_carb",
            "target_protein", "target_fat"
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
