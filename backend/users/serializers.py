from rest_framework import serializers
from .models import CustomUser
from datetime import date
from users.models import UserProfile


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = CustomUser
        fields = ["id", "username", "email", "nickname", "height", "weight", "gender", "age"]
        read_only_fields = ["id"]


class UserProfileUpdateSerializer(serializers.ModelSerializer):
    name = serializers.CharField(required=False)

    class Meta:
        model = CustomUser
        fields = ["name", "nickname", "height", "weight", "gender"]

    def update(self, instance, validated_data):

        # name → nickname 매핑
        if "name" in validated_data:
            instance.nickname = validated_data["name"]

        # 기존 nickname 직접 업데이트도 가능
        if "nickname" in validated_data:
            instance.nickname = validated_data["nickname"]
            
        if "gender" in validated_data:
            instance.gender = validated_data["gender"]

        if "height" in validated_data:
            instance.height = validated_data["height"]

        if "weight" in validated_data:
            instance.weight = validated_data["weight"]

        instance.save()
        return instance
    
    
class FullProfileUpdateSerializer(serializers.Serializer):
    gender = serializers.CharField(required=True)
    birth = serializers.DateField(required=True)
    height = serializers.FloatField(required=True)
    weight = serializers.FloatField(required=True)

    goal_weight = serializers.FloatField(required=True)
    activity_level = serializers.IntegerField(required=True)
    goal_type = serializers.CharField(required=True)  # "lose" | "maintain" | "gain"

    def update(self, user, validated_data):
        # --------------------------
        # 1) User 기본 정보 업데이트
        # --------------------------
        user.gender = validated_data["gender"]
        user.height = validated_data["height"]
        user.weight = validated_data["weight"]

        # 나이 계산
        birth = validated_data["birth"]
        user.age = int((date.today() - birth).days / 365.25)

        user.save()

        # --------------------------
        # 2) UserProfile 업데이트
        # --------------------------
        profile, _ = UserProfile.objects.get_or_create(user=user)
        profile.gender = validated_data["gender"]
        profile.height = validated_data["height"]
        profile.weight = validated_data["weight"]
        profile.age = user.age
        profile.birth = birth
        profile.save()

        # --------------------------
        # 3) UserGoal 업데이트
        # --------------------------
        from goals.models import UserGoal  # ← 반드시 import 필요

        goal, _ = UserGoal.objects.get_or_create(user=user)
        goal.goal_weight = validated_data["goal_weight"]
        goal.activity_level = validated_data["activity_level"]

        goal_type = validated_data["goal_type"]
        if goal_type == "lose":
            goal.goal_type = 1
        elif goal_type == "maintain":
            goal.goal_type = 2
        elif goal_type == "gain":
            goal.goal_type = 3

        goal.save()

        return user
