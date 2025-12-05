# backend/users/views.py
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework_simplejwt.tokens import RefreshToken
from django.utils import timezone
from records.models import MealRecord, MealFood
from goals.models import UserGoal
from datetime import date

from rest_framework import status
from users.serializers import (
    UserSerializer,
    UserProfileUpdateSerializer,
    FullProfileUpdateSerializer,
    
)


class UserDetailView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user

        # 1) 기본 유저 정보
        user_data = UserSerializer(user).data

        # 2) UserGoal (목표 칼로리 / 탄단지)
        goal, _ = UserGoal.objects.get_or_create(user=user)
        goal_data = {
            "kcal": goal.kcal,
            "carbs": goal.carbs,
            "protein": goal.protein,
            "fat": goal.fat,
            "sugar": goal.sugar,
            "goal_weight": goal.goal_weight,
            "activity_level": goal.activity_level,
            "goal_type": goal.goal_type,
        }

        # 3) 오늘 식단 기록
        today_records = MealRecord.objects.filter(
            user=user,
            meal_time__date=date.today()
        )

        # 4) 오늘 먹은 모든 음식
        today_foods = MealFood.objects.filter(
            record__in=today_records
        ).values(
            "food_name", "kcal", "carb", "protein", "fat", "sugar", "amount"
        )

        # 5) 총합 계산
        total = {
            "kcal": sum(f["kcal"] for f in today_foods),
            "carbs": sum(f["carb"] for f in today_foods),
            "protein": sum(f["protein"] for f in today_foods),
            "fat": sum(f["fat"] for f in today_foods),
            "sugar": sum(f["sugar"] for f in today_foods),
        }

        return Response({
            "user": user_data,
            "goal": goal_data,
            "today": {
                "records": list(today_records.values()),
                "foods": list(today_foods),
                "total": total,
            }
        }, status=200)
        
    def patch(self, request):
        user = request.user
        serializer = UserSerializer(user, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(serializer.data, status=200)    
        


class UserFullProfileUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def put(self, request):
        serializer = FullProfileUpdateSerializer(data=request.data)

        if serializer.is_valid():
            # 1) 기본 프로필 업데이트 (gender, height, weight, age 계산)
            user = serializer.update(request.user, serializer.validated_data)

            # -------------------------------------
            # 2) UserGoal 자동 계산 및 업데이트
            # -------------------------------------
            from goals.models import UserGoal
            from goals.views import calculate_daily_targets

            goal, _ = UserGoal.objects.get_or_create(user=user)

            # 목표 계산 실행
            daily = calculate_daily_targets(
                gender=user.gender,
                weight=user.weight,
                height=user.height,
                age=user.age,
                activity=goal.activity_level or 3,
                goal_type=goal.goal_type or 2
            )

            # DB에 저장
            goal.kcal = daily["tdee"]
            goal.carbs = daily["carbs"]
            goal.protein = daily["protein"]
            goal.fat = daily["fat"]
            goal.sugar = daily["sugar"]

            # 목표체중은 기존 값 유지
            # (앱에서 별도로 설정하므로 자동으로 바꾸지 않음)

            goal.save()

            # -------------------------------------

            return Response({
                "message": "프로필 저장 완료 (목표 자동 계산 포함)",
                "user": UserSerializer(user).data
            })

        return Response(serializer.errors, status=400)




class UserDeleteView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        user = request.user
        username = user.username

        # ----------------------------------------------------
        # 1) Refresh Token 가져오기 (쿠키 또는 body)
        # ----------------------------------------------------
        refresh_token = (
            request.COOKIES.get("refresh") or
            request.data.get("refresh") or
            request.headers.get("X-Refresh-Token")
        )

        # ----------------------------------------------------
        # 2) Refresh Token 블랙리스트 처리
        # ----------------------------------------------------
        if refresh_token:
            try:
                token = RefreshToken(refresh_token)
                token.blacklist()
            except Exception:
                pass

        # ----------------------------------------------------
        # 3) Access Token 무효화 (SimpleJWT는 Access는 블랙리스트 불가 → 앱에서 삭제 방식)
        #    → 서버에서는 실제로 Access Token을 “사용 불가”로 만드는 방식 없음.
        #    → 대신 유저 자체를 삭제하므로 AccessToken은 인증 단계에서 무조건 실패됨.
        # ----------------------------------------------------

        # ----------------------------------------------------
        # 4) 유저 및 연관 DB 삭제
        # ----------------------------------------------------
        user.delete()

        # ----------------------------------------------------
        # 5) 쿠키 삭제 (웹 환경 대비)
        # ----------------------------------------------------
        response = Response(
            {"message": f"사용자 '{username}' 계정이 삭제되었습니다.", "status": "success"},
            status=200
        )
        response.delete_cookie("access")
        response.delete_cookie("refresh")

        return response