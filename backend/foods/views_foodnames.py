from rest_framework.views import APIView
from rest_framework.response import Response
import yaml
import os
from django.conf import settings

class FoodNameListAPIView(APIView):
    def get(self, request):
        model_yaml_path = os.path.join(settings.BASE_DIR, "ai_inference", "model", "food_model.yaml")

        if not os.path.exists(model_yaml_path):
            return Response({"success": False, "error": "model yaml not found"}, status=500)

        with open(model_yaml_path, "r", encoding="utf-8") as f:
            data = yaml.safe_load(f)

        names = data.get("names", [])
        return Response({"success": True, "names": names}, status=200)
