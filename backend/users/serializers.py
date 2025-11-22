from rest_framework import serializers
from .models import CustomUser, UserProfile
from datetime import date


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
