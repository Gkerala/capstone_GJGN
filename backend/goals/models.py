# goals/models.py
from django.db import models
from django.conf import settings

class UserGoal(models.Model):
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)

    goal_type = models.IntegerField(default=2)  # 1 감량 / 2 유지 / 3 증가
    goal_weight = models.FloatField(null=True, blank=True)

    activity_level = models.IntegerField(default=3)  # 1~5

    kcal = models.IntegerField(default=2000)
    carbs = models.IntegerField(default=250)
    protein = models.IntegerField(default=120)
    fat = models.IntegerField(default=60)
    sugar = models.IntegerField(default=50)  # WHO 기준 10%

    # 자동 생성 모드
    auto_mode = models.BooleanField(default=True)

    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.user.username} Goal"
