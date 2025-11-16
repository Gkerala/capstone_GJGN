from rest_framework import serializers
from .models import Food, FoodAnalysis


class FoodSerializer(serializers.ModelSerializer):
    class Meta:
        model = Food
        fields = "__all__"


class FoodAnalysisSerializer(serializers.ModelSerializer):
    food = FoodSerializer(read_only=True)

    class Meta:
        model = FoodAnalysis
        fields = "__all__"
