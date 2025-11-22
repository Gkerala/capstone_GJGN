# backend/ai_inference/views.py
import torch
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from ultralytics import YOLO
from PIL import Image

class FoodDetectView(APIView):

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        print("🔥 YOLO 모델 로딩 중...")
        self.model = YOLO("ai_inference/yolo_model/best.pt")   # 모델 경로
        print("🔥 YOLO 모델 로딩 완료")

    def post(self, request):
        if "image" not in request.FILES:
            return Response({"success": False, "error": "이미지가 필요합니다."},
                            status=status.HTTP_400_BAD_REQUEST)

        image_file = request.FILES["image"]

        try:
            img = Image.open(image_file).convert("RGB")
        except:
            return Response({"success": False, "error": "이미지 로딩 실패"},
                            status=status.HTTP_400_BAD_REQUEST)

        # YOLO 예측
        results = self.model(img)
        names = self.model.names

        detections = []

        for r in results:
            for box in r.boxes:
                cls = int(box.cls[0])                      # class index
                conf = float(box.conf[0])                 # confidence
                xyxy = box.xyxy[0].tolist()               # [x1, y1, x2, y2]

                detections.append({
                    "name": names[cls],
                    "confidence": round(conf, 4),
                    "bbox": xyxy
                })

        return Response({
            "success": True,
            "count": len(detections),      # 🔥 다중객체 감지 개수 추가
            "foods": detections            # 🔥 단일/다중 객체 모두 이 리스트만 파싱
        }, status=200)
