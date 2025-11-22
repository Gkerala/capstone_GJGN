from django.contrib.auth.models import AbstractUser
from django.db import models
from django.conf import settings
from django.contrib.auth import get_user_model

def profile_upload_path(instance, filename):
    return f"profile/{instance.id}/{filename}"


class CustomUser(AbstractUser):
    kakao_id = models.CharField(max_length=255, unique=True, null=True, blank=True)
    nickname = models.CharField(max_length=50, blank=True)
    profile_image = models.URLField(blank=True, null=True)

    email = models.EmailField(null=True, blank=True)

    # 기본 신체 정보
    height = models.FloatField(null=True, blank=True)
    weight = models.FloatField(null=True, blank=True)
    gender = models.CharField(max_length=10, null=True, blank=True)
    age = models.IntegerField(null=True, blank=True)

    def __str__(self):
        return self.username


User = get_user_model()


class UserProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)

    gender = models.CharField(max_length=10, null=True, blank=True)
    height = models.FloatField(null=True, blank=True)
    weight = models.FloatField(null=True, blank=True)
    age = models.IntegerField(null=True, blank=True)

    # 목표 모드만 유지
    goal_mode = models.CharField(max_length=20, default="maintain")


class UserGoal(models.Model):
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)

    # 새로 추가된 필드
    goal_type = models.IntegerField(null=True, blank=True)      # 1,2,3 (유지/감량/증량)
    goal_weight = models.FloatField(null=True, blank=True)       # 목표 체중
    activity_level = models.IntegerField(default=3)              # 1~5 값

    # 기존 목표량
    target_kcal = models.IntegerField(default=2000)
    target_carb = models.IntegerField(default=250)
    target_protein = models.IntegerField(default=70)
    target_fat = models.IntegerField(default=60)

    auto_mode = models.BooleanField(default=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.user.username} Goal"
