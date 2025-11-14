import os
from pathlib import Path
from datetime import timedelta
from dotenv import load_dotenv

# .env 로드
load_dotenv()

BASE_DIR = Path(__file__).resolve().parent.parent

# -----------------------------
# 🔐 ENV 환경변수 기반 설정
# -----------------------------
SECRET_KEY = os.getenv("SECRET_KEY", "default-secret-key")

DEBUG = True

ALLOWED_HOSTS = ["*"]

# -----------------------------
# Installed Apps
# -----------------------------
INSTALLED_APPS = [
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "django.contrib.staticfiles",

    # Third-party
    "rest_framework",
    "rest_framework_simplejwt",
    "corsheaders",

    # Apps
    "users",
    "foods",
    "records",
    "goals",
]

AUTH_USER_MODEL = "users.CustomUser"

# -----------------------------
# Middleware
# -----------------------------
MIDDLEWARE = [
    "corsheaders.middleware.CorsMiddleware",
    "django.middleware.security.SecurityMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
]

CORS_ALLOW_ALL_ORIGINS = True

ROOT_URLCONF = "SmartDiet_backend.urls"

# -----------------------------
# REST Framework + JWT
# -----------------------------
REST_FRAMEWORK = {
    "DEFAULT_AUTHENTICATION_CLASSES": (
        "rest_framework_simplejwt.authentication.JWTAuthentication",
    ),
}

SIMPLE_JWT = {
    "ACCESS_TOKEN_LIFETIME": timedelta(days=3),
    "REFRESH_TOKEN_LIFETIME": timedelta(days=14),
    "SIGNING_KEY": os.getenv("JWT_SECRET", SECRET_KEY),
}

# -----------------------------
# Database (MySQL)
# -----------------------------
DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.mysql",
        "NAME": os.getenv("DB_NAME"),
        "USER": os.getenv("DB_USER"),
        "PASSWORD": os.getenv("DB_PASSWORD"),
        "HOST": os.getenv("DB_HOST", "localhost"),
        "PORT": os.getenv("DB_PORT", "3306"),
        "OPTIONS": {"charset": "utf8mb4"},
    }
}

# -----------------------------
# Static & Media
# -----------------------------
STATIC_URL = "/static/"

MEDIA_URL = "/media/"
MEDIA_ROOT = BASE_DIR / "media"

# -----------------------------
# Locale
# -----------------------------
LANGUAGE_CODE = "ko-kr"
TIME_ZONE = "Asia/Seoul"
USE_I18N = True
USE_TZ = True

WSGI_APPLICATION = "SmartDiet_backend.wsgi.application"
ASGI_APPLICATION = "SmartDiet_backend.asgi.application"

AI_INFERENCE_URL = config("AI_INFERENCE_URL", default="http://localhost:8001/predict")
