# backend/records/serializers.py
from rest_framework import serializers
from .models import MealRecord, MealFood, WeightRecord


# --------------------------------------------------------
# 1) MealFoodSerializer (Android 구조 그대로 받는 버전)
# --------------------------------------------------------
class MealFoodSerializer(serializers.ModelSerializer):
    class Meta:
        model = MealFood
        fields = [
            "food_name",
            "amount",
            "kcal",
            "carb",
            "protein",
            "fat",
            "sugar",
        ]


# --------------------------------------------------------
# 2) MealRecord 생성용 serializer
# --------------------------------------------------------
class MealRecordCreateSerializer(serializers.ModelSerializer):
    foods = MealFoodSerializer(many=True)

    class Meta:
        model = MealRecord
        fields = ["meal_type", "memo", "image", "foods"]

    def create(self, validated_data):
        foods_data = validated_data.pop("foods")
        user = self.context["request"].user

        # MealRecord 생성
        record = MealRecord.objects.create(user=user, **validated_data)

        # 관련된 Foods 생성
        for item in foods_data:
            MealFood.objects.create(
                record=record,
                food_name=item["food_name"],
                amount=item["amount"],
                kcal=item["kcal"],
                carb=item["carb"],
                protein=item["protein"],
                fat=item["fat"],
                sugar=item["sugar"],
            )

        return record


# --------------------------------------------------------
# 3) MealRecord 리스트용
# --------------------------------------------------------
class MealRecordListSerializer(serializers.ModelSerializer):
    class Meta:
        model = MealRecord
        fields = ["id", "meal_time", "image", "created_at"]


# --------------------------------------------------------
# 4) MealRecord 상세 조회용
# --------------------------------------------------------
class MealRecordDetailSerializer(serializers.ModelSerializer):
    foods = MealFoodSerializer(many=True)

    class Meta:
        model = MealRecord
        fields = [
            "id",
            "meal_time",
            "memo",
            "image",
            "foods",
            "created_at",
        ]


# --------------------------------------------------------
# 5) 공통 serializer
# --------------------------------------------------------
class MealRecordSerializer(serializers.ModelSerializer):
    foods = MealFoodSerializer(many=True)

    class Meta:
        model = MealRecord
        fields = [
            "id",
            "meal_time",
            "memo",
            "image",
            "foods",
            "created_at",
        ]

class WeightRecordCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = WeightRecord
        fields = ["weight", "date"]  # 🔥 memo 삭제, recorded_at 삭제

    def create(self, validated_data):
        user = self.context["request"].user
        return WeightRecord.objects.create(user=user, **validated_data)


class WeightRecordSerializer(serializers.ModelSerializer):
    class Meta:
        model = WeightRecord
        fields = ["id", "weight", "date", "created_at"]

# --------------------------------------------------------
# 🔍 날짜별 통계
# --------------------------------------------------------
class DailyStatSerializer(serializers.Serializer):
    date = serializers.DateField()
    total_calories = serializers.FloatField()


# --------------------------------------------------------
# 📊 주간 칼로리 분석
# --------------------------------------------------------
class WeeklyDayCaloriesSerializer(serializers.Serializer):
    date = serializers.DateField()
    calories = serializers.FloatField()


class WeeklyAnalysisSerializer(serializers.Serializer):
    week_start = serializers.DateField()
    week_end = serializers.DateField()
    weekly_records = WeeklyDayCaloriesSerializer(many=True)


# --------------------------------------------------------
# ⚖️ 주간 체중 변화
# --------------------------------------------------------
class WeeklyWeightItemSerializer(serializers.Serializer):
    date = serializers.DateField()
    weight = serializers.FloatField()


class WeeklyWeightSerializer(serializers.Serializer):
    week_start = serializers.DateField()
    week_end = serializers.DateField()
    records = WeeklyWeightItemSerializer(many=True)
    
    
class MealFoodSerializer(serializers.ModelSerializer):
    class Meta:
        model = MealFood
        fields = ["id", "food_name", "amount", "kcal", "carb", "protein", "fat", "sugar"]

class MealRecordSerializer(serializers.ModelSerializer):
    foods = MealFoodSerializer(many=True)

    class Meta:
        model = MealRecord
        fields = ["id", "meal_type", "meal_time", "foods"]
