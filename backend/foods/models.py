from django.db import models


class Food(models.Model):
    """음식 기본 데이터"""
    name = models.CharField(max_length=100, unique=True)
    image = models.ImageField(upload_to="foods/", null=True, blank=True)

    calories = models.FloatField(default=0)
    protein = models.FloatField(default=0)
    carbs = models.FloatField(default=0)
    fat = models.FloatField(default=0)

    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.name


class FoodAnalysis(models.Model):
    """AI 분석 결과 저장"""
    food = models.ForeignKey(Food, on_delete=models.CASCADE, related_name="analyses")
    image = models.ImageField(upload_to="foods/analysis/")
    calories = models.FloatField()
    protein = models.FloatField()
    carbs = models.FloatField()
    fat = models.FloatField()

    confidence = models.FloatField(default=0.0)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Analysis for {self.food.name} @ {self.created_at}"
