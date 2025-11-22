# backend/records/models.py
from django.db import models
from django.conf import settings


class MealRecord(models.Model):
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="meal_records"
    )
    meal_time = models.DateTimeField()
    memo = models.TextField(blank=True, null=True)
    image = models.ImageField(upload_to="records/", blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.user.email} - {self.meal_time}"


class MealFood(models.Model):
    record = models.ForeignKey(
        MealRecord,
        on_delete=models.CASCADE,
        related_name="foods"
    )
    name = models.CharField(max_length=255)
    amount = models.FloatField(default=1.0)

    # 영양정보
    kcal = models.FloatField(default=0)
    carb = models.FloatField(default=0)
    protein = models.FloatField(default=0)
    fat = models.FloatField(default=0)
    sugar = models.FloatField(default=0)

    def __str__(self):
        return f"{self.record.id} - {self.name}"
