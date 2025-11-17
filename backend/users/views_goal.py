from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status

from .models import UserGoal
from .serializers import UserGoalUpdateSerializer


class UserGoalUpdateAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request):
        user = request.user

        try:
            goal = UserGoal.objects.get(user=user)
        except UserGoal.DoesNotExist:
            return Response({"error": "목표 설정이 먼저 필요합니다."}, status=404)

        serializer = UserGoalUpdateSerializer(goal, data=request.data, partial=True)

        if serializer.is_valid():
            serializer.save(auto_mode=False)   # 수동 변경으로 전환
            return Response({
                "message": "목표가 성공적으로 수정되었습니다.",
                "goal": serializer.data
            })
            
        return Response(serializer.errors, status=400)

class UserGoalRetrieveAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        try:
            goal = UserGoal.objects.get(user=user)
        except UserGoal.DoesNotExist:
            return Response({"error": "목표 정보가 없습니다."}, status=404)

        data = {
            "target_kcal": goal.target_kcal,
            "target_carb": goal.target_carb,
            "target_protein": goal.target_protein,
            "target_fat": goal.target_fat,
            "auto_mode": goal.auto_mode,
            "updated_at": goal.updated_at,
        }

        return Response(data, status=200)