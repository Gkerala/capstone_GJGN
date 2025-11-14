from django.db import models
from django.conf import settings
from foods.models import Food


class MealRecord(models.Model):
    """사용자 1명 → 하루 여러 개의 식단 기록"""
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="meal_records"
    )
    meal_time = models.DateTimeField()  # 식사 시각
    memo = models.TextField(blank=True, null=True)
    image = models.ImageField(upload_to="records/", blank=True, null=True)

    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.user.email} - {self.meal_time}"


class MealFood(models.Model):
    """식단 기록에 포함된 개별 음식"""
    record = models.ForeignKey(
        MealRecord,
        on_delete=models.CASCADE,
        related_name="foods"
    )
    food = models.ForeignKey(Food, on_delete=models.PROTECT)  # 음식 DB 참조
    amount = models.FloatField(default=1.0)  # 1인분 단위 비율
    calories = models.FloatField(default=0)
    carbs = models.FloatField(default=0)
    protein = models.FloatField(default=0)
    fat = models.FloatField(default=0)

    def __str__(self):
        return f"{self.record.id} - {self.food.name}"

class Weight(models.Model):
    user = models.ForeignKey("users.CustomUser", on_delete=models.CASCADE)
    weight = models.FloatField()  # kg
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at"]
