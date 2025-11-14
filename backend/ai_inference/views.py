# ai_inference/views.py
import torch
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from ultralytics import YOLO
from PIL import Image
import io

class FoodDetectView(APIView):

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        print("🔥 YOLO 모델 로딩 중...")
        self.model = YOLO("ai_inference/yolo_model/best.pt")   # 경로 주의
        print("🔥 YOLO 모델 로딩 완료")

    def post(self, request):
        if "image" not in request.FILES:
            return Response({"error": "이미지가 필요합니다."},
                            status=status.HTTP_400_BAD_REQUEST)

        image_file = request.FILES["image"]

        try:
            img = Image.open(image_file).convert("RGB")
        except:
            return Response({"error": "이미지 로딩 실패"},
                            status=status.HTTP_400_BAD_REQUEST)

        results = self.model(img)

        detections = []
        for r in results:
            boxes = r.boxes
            names = self.model.names

            for box in boxes:
                cls = int(box.cls[0])
                conf = float(box.conf[0])
                xyxy = box.xyxy[0].tolist()

                detections.append({
                    "label": names[cls],
                    "confidence": round(conf, 4),
                    "box": {
                        "x1": xyxy[0],
                        "y1": xyxy[1],
                        "x2": xyxy[2],
                        "y2": xyxy[3]
                    }
                })

        return Response({
            "status": "success",
            "count": len(detections),
            "items": detections
        }, status=200)
