from django.db import models
from django.contrib.auth.models import User


# 사용자 프로필 정보
class UserInfo(models.Model):
    ACTIVITY_CHOICES = [
        ('low', '낮음'),
        ('medium', '보통'),
        ('high', '활발함'),
    ]

    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='profile')
    name = models.CharField(max_length=50)
    birth_date = models.DateField(null=True, blank=True)
    gender = models.CharField(max_length=10, choices=[('M', '남성'), ('F', '여성')], blank=True)
    height_cm = models.FloatField(null=True, blank=True)
    weight_kg = models.FloatField(null=True, blank=True)
    goal_weight = models.FloatField(null=True, blank=True)
    activity_level = models.CharField(max_length=10, choices=ACTIVITY_CHOICES, default='medium')

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.user.username}의 프로필"


# 체중 기록
class WeightRecord(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='weight_records')
    weight = models.FloatField()
    date = models.DateField(auto_now_add=True)

    class Meta:
        ordering = ['-date']

    def __str__(self):
        return f"{self.user.username} - {self.weight}kg ({self.date})"


# 식단 기록
class DietRecord(models.Model):
    MEAL_CHOICES = [
        ('morning', '아침'),
        ('lunch', '점심'),
        ('dinner', '저녁'),
    ]

    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='diet_records')
    image = models.ImageField(upload_to='diet_images/')
    total_calories = models.FloatField(default=0.0)
    meal_type = models.CharField(max_length=10, choices=MEAL_CHOICES, default='morning')
    date = models.DateTimeField(auto_now_add=True)

    # YOLO 감지된 세부 음식 정보 (JSON 형태)
    details = models.JSONField(default=dict, blank=True)

    class Meta:
        ordering = ['-date']

    def __str__(self):
        return f"{self.user.username} - {self.meal_type} ({self.date.date()})"


# 음식 칼로리 DB (선택적, 사전 정의용)
class FoodCalorie(models.Model):
    name = models.CharField(max_length=100, unique=True)
    calories_per_100g = models.FloatField()
    protein = models.FloatField(default=0.0)
    fat = models.FloatField(default=0.0)
    carbs = models.FloatField(default=0.0)

    def __str__(self):
        return f"{self.name} ({self.calories_per_100g} kcal/100g)"
