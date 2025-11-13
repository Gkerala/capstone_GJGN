"""
URL configuration for diet_app project.
"""

from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static

# --- REST Framework & JWT 관련 ---
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView

# --- core 앱의 views import ---
from core.views import (
    UserInfoViewSet,
    FoodNutritionViewSet,
    DietRecordViewSet,
    kakao_login,
    logout_user,
    delete_user_account,
    CurrentUserView,   # ✅ 클래스형 CurrentUserView 사용
)

# --- ① DRF 라우터 설정 ---
router = DefaultRouter()
router.register(r'users', UserInfoViewSet, basename='users')
router.register(r'foods', FoodNutritionViewSet, basename='foods')
router.register(r'records', DietRecordViewSet, basename='records')

# --- ② URL 패턴 정의 ---
urlpatterns = [
    # 관리자 페이지
    path('admin/', admin.site.urls),

    # ✅ 인증 관련
    path('api/auth/kakao/', kakao_login, name='kakao_login'),
    path('api/auth/logout/', logout_user, name='logout_user'),
    path('api/auth/delete/', delete_user_account, name='delete_user_account'),

    # ✅ 로그인한 사용자의 본인 프로필 조회 (router보다 위)
    path('api/users/me/', CurrentUserView.as_view(), name='current_user'),

    # ✅ JWT 토큰 발급 및 갱신
    path('api/token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('api/token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),

    # ✅ 나머지 기본 REST 엔드포인트 (항상 마지막)
    path('api/', include(router.urls)),
]

# --- ③ 개발 환경용 Media 파일 서빙 ---
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
