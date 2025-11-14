from django.contrib.auth.models import AbstractUser
from django.db import models
from django.conf import settings
from django.contrib.auth import get_user_model


# -----------------------------
# 프로필 이미지 업로드 경로
# -----------------------------
def profile_upload_path(instance, filename):
    return f"profile/{instance.id}/{filename}"


# -----------------------------
# CustomUser 모델
# -----------------------------
class CustomUser(AbstractUser):
    kakao_id = models.CharField(max_length=255, unique=True, null=True, blank=True)
    nickname = models.CharField(max_length=50, blank=True)
    profile_image = models.ImageField(upload_to=profile_upload_path, null=True, blank=True)
    
    # 기초 정보
    height = models.FloatField(null=True, blank=True)   # cm
    weight = models.FloatField(null=True, blank=True)
    gender = models.CharField(max_length=10, null=True, blank=True)
    age = models.IntegerField(null=True, blank=True)
    activity_level = models.FloatField(default=1.2)  # 1.2 ~ 1.9

    # 목표 기본값
    target_calories = models.IntegerField(default=2000)
    target_carbs = models.IntegerField(default=250)
    target_protein = models.IntegerField(default=75)
    target_fat = models.IntegerField(default=60)

    def __str__(self):
        return self.username


# -----------------------------
# UserProfile 모델
# -----------------------------
User = get_user_model()   # <-- CustomUser 자동 연결됨

class UserProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)

    gender = models.CharField(max_length=10)
    height = models.FloatField()
    weight = models.FloatField()
    age = models.IntegerField()
    activity_level = models.CharField(max_length=20)   # sedentary, light, ...

    goal_mode = models.CharField(max_length=20, default="maintain")  
    # maintain / lose / gain

    # 자동 목표 계산 결과
    goal_calories = models.IntegerField(null=True, blank=True)
    goal_carbs = models.IntegerField(null=True, blank=True)
    goal_protein = models.IntegerField(null=True, blank=True)
    goal_fat = models.IntegerField(null=True, blank=True)

    def __str__(self):
        return f"{self.user.username} Profile"


# -----------------------------
# UserGoal 모델
# -----------------------------
class UserGoal(models.Model):
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)

    target_kcal = models.IntegerField(default=2000)

    target_carb = models.IntegerField(default=250)
    target_protein = models.IntegerField(default=70)
    target_fat = models.IntegerField(default=60)

    auto_mode = models.BooleanField(default=True)

    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.user.username} Goal"
 