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

