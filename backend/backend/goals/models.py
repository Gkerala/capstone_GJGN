# goals/models.py

from django.db import models
from django.conf import settings


class NutritionGoal(models.Model):
    """
    사용자의 일일 섭취 목표 (칼로리/탄단지)
    """
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)

    # 일일 목표값
    calorie = models.FloatField(default=2000)
    protein = models.FloatField(default=150)
    carbs = models.FloatField(default=250)
    fat = models.FloatField(default=70)

    # 자동 생성 옵션
    bmr = models.FloatField(null=True, blank=True)  # 기초대사량
    activity_level = models.CharField(max_length=20, default="normal")

    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.user.username} Daily Nutrition Goal"


class WeightRecord(models.Model):
    """
    사용자의 체중 기록
    """
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    weight = models.FloatField()  # kg
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at"]  # 최신 내림차순

    def __str__(self):
        return f"{self.user.username} - {self.weight}kg ({self.created_at})"
