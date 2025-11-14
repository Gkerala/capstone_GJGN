# goals/models.py

from django.db import models
from django.conf import settings

class Goal(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)

    # 총 칼로리 목표
    calories = models.FloatField(default=2000)

    # 주요 3대 영양소 목표(g)
    carbs_g = models.FloatField(default=250)     # 탄수화물
    protein_g = models.FloatField(default=100)   # 단백질
    fat_g = models.FloatField(default=50)        # 지방

    # 자동 계산된 목표인지 여부
    auto_generated = models.BooleanField(default=True)

    # 최신 목표가 우선되도록
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.user} goal ({self.calories} kcal)"
    
class NutritionGoal(models.Model):
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)


    # 일일 목표
    calorie = models.IntegerField(default=2000)
    protein = models.FloatField(default=150)
    carbs = models.FloatField(default=250)
    fat = models.FloatField(default=70)


    # 자동 생성 정보
    bmr = models.FloatField(null=True, blank=True)
    activity_level = models.CharField(max_length=20, default="normal")


    updated_at = models.DateTimeField(auto_now=True)


    def __str__(self):
        return f"{self.user.email} 목표"
