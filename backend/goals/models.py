# goals/models.py
from django.db import models
from django.conf import settings

class WeightRecord(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    weight = models.FloatField()
    created_at = models.DateTimeField()

    class Meta:
        db_table = "records_weight"
        ordering = ["-created_at"]

    def __str__(self):
        return f"{self.user} - {self.weight}kg ({self.created_at})"


class UserGoal(models.Model):
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)

    goal_type = models.IntegerField(default=2)  # 1 감량 / 2 유지 / 3 증가
    goal_weight = models.FloatField(null=True, blank=True)

    # 활동량: 1~5
    activity_level = models.IntegerField(default=3)

    # 자동 계산된 목표 값
    kcal = models.IntegerField(default=2000)
    carbs = models.IntegerField(default=250)
    protein = models.IntegerField(default=120)
    fat = models.IntegerField(default=60)
    sugar = models.FloatField(default=50)

    auto_mode = models.BooleanField(default=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.user.username} Goal"

