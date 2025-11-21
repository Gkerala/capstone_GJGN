from rest_framework import serializers
from .models import CustomUser, UserGoal, UserProfile
from datetime import date

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

class UserProfileUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = CustomUser
        fields = ["nickname", "height", "weight", "profile_image"]
        
class FullProfileUpdateSerializer(serializers.Serializer):
    gender = serializers.CharField(required=True)
    birth = serializers.DateField(required=True)
    height = serializers.FloatField(required=True)
    weight = serializers.FloatField(required=True)
    target_weight = serializers.FloatField(required=True)
    activity_level = serializers.IntegerField(required=True)
    goal_type = serializers.CharField(required=True)

    def update(self, user, validated_data):

        # 1) 기본 CustomUser 정보 저장
        user.gender = validated_data["gender"]
        user.height = validated_data["height"]
        user.weight = validated_data["weight"]

        # 나이 계산
        birth = validated_data["birth"]
        user.age = int((date.today() - birth).days / 365.25)

        user.save()

        # 2) UserProfile 저장 또는 생성
        profile, _ = UserProfile.objects.get_or_create(user=user)
        profile.gender = validated_data["gender"]
        profile.height = validated_data["height"]
        profile.weight = validated_data["weight"]
        profile.age = user.age
        profile.activity_level = validated_data["activity_level"]
        profile.goal_mode = validated_data["goal_type"]
        profile.save()

        # 3) 목표 저장(UserGoal)
        goal, _ = UserGoal.objects.get_or_create(user=user)
        goal.target_kcal = 2000  # 기본값(추후 계산 가능)
        goal.target_carb = 250
        goal.target_protein = 75
        goal.target_fat = 60
        goal.save()

        return user
