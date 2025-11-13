from django.db import models
from datetime import date as default_date
from django.contrib.auth.models import User


class UserInfo(models.Model):
    # ✅ Django 기본 User 모델과 1:1 연결 (User 삭제 시 자동 삭제)
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name="userinfo")

    # ✅ 앱에서 관리하는 확장 정보
    username = models.CharField(max_length=50)
    email = models.EmailField(max_length=100, unique=True, blank=True, null=True)
    password = models.CharField(max_length=255, blank=True, null=True)

    birth_date = models.DateField(blank=True, null=True)
    gender = models.CharField(
        max_length=1,
        choices=[('M', '남성'), ('F', '여성'), ('O', '기타')],
        blank=True,
        null=True
    )
    height_cm = models.FloatField(blank=True, null=True)
    weight_kg = models.FloatField(blank=True, null=True)

    goal_type = models.CharField(
        max_length=10,
        choices=[('loss', '감량'), ('maintain', '유지'), ('gain', '증량')],
        blank=True,
        null=True
    )
    target_weight_kg = models.FloatField(blank=True, null=True)
    activity_level = models.CharField(
        max_length=10,
        choices=[('low', '비활동적'), ('moderate', '보통'), ('high', '활동적')],
        blank=True,
        null=True
    )

    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.user.username} ({self.username})"


class FoodNutrition(models.Model):
    food_name = models.CharField(max_length=100)
    calories = models.FloatField()
    carbohydrate = models.FloatField()
    protein = models.FloatField()
    fat = models.FloatField()

    def __str__(self):
        return self.food_name


class DietRecord(models.Model):
    user = models.ForeignKey(UserInfo, on_delete=models.CASCADE, related_name="diet_records")
    food = models.ForeignKey(FoodNutrition, on_delete=models.CASCADE, related_name="food_records")
    date = models.DateField(default=default_date.today)
    meal_type = models.CharField(
        max_length=20,
        choices=[
            ('아침', '아침'),
            ('점심', '점심'),
            ('저녁', '저녁'),
            ('간식', '간식')
        ]
    )
    portion = models.FloatField(default=1.0)

    def __str__(self):
        return f"{self.user.username} - {self.food.food_name} ({self.meal_type})"
