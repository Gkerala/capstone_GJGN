# goals/models.py
from django.db import models
from django.conf import settings


class NutritionGoal(models.Model):
    """사용자 일일 영양 목표 (칼로리, 탄단지)"""
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)

    calorie = models.FloatField(default=2000)
    protein = models.FloatField(default=150)
    carbs = models.FloatField(default=250)
    fat = models.FloatField(default=70)

    bmr = models.FloatField(null=True, blank=True)
    activity_level = models.CharField(max_length=20, default="normal")

    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.user.username} Nutrition Goal"


class WeightRecord(models.Model):
    """사용자 체중 기록"""
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    weight = models.FloatField()
    created_at = models.DateTimeField()

    class Meta:
        db_table = "records_weight"
        ordering = ["-created_at"]

    def __str__(self):
        return f"{self.user} - {self.weight}kg ({self.created_at})"
