from datetime import datetime
from rest_framework import serializers
from django.contrib.auth.models import User
from .models import UserInfo, FoodNutrition, DietRecord


# ✅ YYYY-MM-DD 또는 YYYYMMDD 모두 허용하는 커스텀 DateField
class FlexibleDateField(serializers.DateField):
    def to_internal_value(self, value):
        if isinstance(value, str):
            if len(value) == 8 and value.isdigit():
                try:
                    value = datetime.strptime(value, "%Y%m%d").date()
                except ValueError:
                    raise serializers.ValidationError("날짜 형식이 잘못되었습니다. 예: 20020112")
        return super().to_internal_value(value)


# ✅ Django 기본 User 모델 직렬화
class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'username', 'email']


# ✅ 사용자 정보 직렬화 (UserInfo)
class UserInfoSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)  # 읽기 전용
    user_id = serializers.PrimaryKeyRelatedField(  # 쓰기용
        queryset=User.objects.all(),
        source='user',
        write_only=True
    )

    birth_date = FlexibleDateField(required=False, allow_null=True)
    # ✅ 추가: 프로필 완성 여부
    profile_completed = serializers.SerializerMethodField()

    class Meta:
        model = UserInfo
        fields = [
            'id', 'user', 'user_id',
            'username', 'email', 'birth_date', 'gender',
            'height_cm', 'weight_kg', 'goal_type', 'target_weight_kg',
            'activity_level', 'created_at', 'profile_completed'
        ]

    def get_profile_completed(self, obj):
        """필수 필드가 모두 채워져 있으면 True"""
        required_fields = [obj.height_cm, obj.weight_kg, obj.goal_type]
        return all(required_fields)


# ✅ 음식 영양소 직렬화
class FoodNutritionSerializer(serializers.ModelSerializer):
    class Meta:
        model = FoodNutrition
        fields = ['id', 'food_name', 'calories', 'carbohydrate', 'protein', 'fat']


# ✅ 식단 기록 직렬화
class DietRecordSerializer(serializers.ModelSerializer):
    user = UserInfoSerializer(read_only=True)
    user_id = serializers.PrimaryKeyRelatedField(write_only=True, source='user', queryset=UserInfo.objects.all())
    food = FoodNutritionSerializer(read_only=True)
    food_id = serializers.PrimaryKeyRelatedField(write_only=True, source='food', queryset=FoodNutrition.objects.all())

    class Meta:
        model = DietRecord
        fields = [
            'id', 'user', 'user_id', 'food', 'food_id',
            'date', 'meal_type', 'portion'
        ]
        read_only_fields = ('id',)


# ✅ 카카오 로그인 응답 직렬화
class KakaoLoginResponseSerializer(serializers.Serializer):
    access = serializers.CharField()
    refresh = serializers.CharField()
    user = UserInfoSerializer()
