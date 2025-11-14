from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('admin/', admin.site.urls),

    path('api/auth/', include('users.urls_auth')),   # 카카오 로그인
    path('api/users/', include('users.urls')),       # 사용자 정보
    path('api/foods/', include('foods.urls')),       # 음식 분석
    path('api/records/', include('records.urls')),   # 식단 기록
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
