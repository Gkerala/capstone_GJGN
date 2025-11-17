from rest_framework import serializers
from .models import CustomUser, UserGoal

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = CustomUser
        fields = ["id", "username", "email", "profile_image", "kakao_id"]
        read_only_fields = ["id", "kakao_id"]

class UserGoalSerializer(serializers.ModelSerializer):
    class Meta:
        model = CustomUser
        fields = [
            "target_calories", "target_carbs", "target_protein", "target_fat"
        ]


class UserGoalUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserGoal
        fields = (
            "target_kcal",
            "target_carb",
            "target_protein",
            "target_fat",
        )

    def validate(self, data):
        # 최소 kcal 제한
        kcal = data.get("target_kcal")
        if kcal is not None and kcal < 800:
            raise serializers.ValidationError("하루 목표 칼로리는 최소 800 이상이어야 합니다.")

        return data
