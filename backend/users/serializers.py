from rest_framework import serializers
from .models import CustomUser, UserGoal, UserProfile
from datetime import date


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = CustomUser
        fields = ["id", "username", "email", "nickname", "height", "weight", "gender", "age", "profile_image"]
        read_only_fields = ["id"]


class UserProfileUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = CustomUser
        fields = ["nickname", "height", "weight", "profile_image"]


class UserGoalUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserGoal
        fields = [
            "goal_type",
            "goal_weight",
            "activity_level",
            "target_kcal",
            "target_carb",
            "target_protein",
            "target_fat",
        ]

    def validate(self, data):
        kcal = data.get("target_kcal")
        if kcal is not None and kcal < 800:
            raise serializers.ValidationError("하루 목표 칼로리는 최소 800 이상이어야 합니다.")
        return data


class FullProfileUpdateSerializer(serializers.Serializer):
    gender = serializers.CharField(required=True)
    birth = serializers.DateField(required=True)
    height = serializers.FloatField(required=True)
    weight = serializers.FloatField(required=True)

    def update(self, user, validated_data):

        user.gender = validated_data["gender"]
        user.height = validated_data["height"]
        user.weight = validated_data["weight"]

        birth = validated_data["birth"]
        user.age = int((date.today() - birth).days / 365.25)

        user.save()

        profile, _ = UserProfile.objects.get_or_create(user=user)
        profile.gender = validated_data["gender"]
        profile.height = validated_data["height"]
        profile.weight = validated_data["weight"]
        profile.age = user.age
        profile.save()

        return user
