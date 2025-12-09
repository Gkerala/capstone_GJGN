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
        self.model = YOLO("ai_inference/yolo_model/best.pt")
        print("🔥 YOLO 모델 로딩 완료")

    def post(self, request):

        # ------------------------------------------------------------
        # 1) 이미지 체크
        # ------------------------------------------------------------
        if "image" not in request.FILES:
            return Response({
                "success": False,
                "error": "이미지가 필요합니다."
            }, status=status.HTTP_400_BAD_REQUEST)

        image_file = request.FILES["image"]

        # ------------------------------------------------------------
        # 2) 이미지 로딩
        # ------------------------------------------------------------
        try:
            img = Image.open(image_file).convert("RGB")
        except Exception as e:
            print("🟥 이미지 로딩 오류:", e)
            return Response({
                "success": False,
                "error": "이미지 로딩 실패"
            }, status=status.HTTP_400_BAD_REQUEST)

        # ------------------------------------------------------------
        # 3) YOLO 예측
        # ------------------------------------------------------------
        try:
            results = self.model(img)
        except Exception as e:
            print("🟥 YOLO 예측 중 오류:", e)
            return Response({
                "success": False,
                "error": "YOLO 예측 실패"
            }, status=500)

        names = self.model.names
        print("🟦 YOLO model.names:", names)

        detections = []

        # ------------------------------------------------------------
        # 4) 박스 파싱
        # ------------------------------------------------------------
        try:
            for r in results:
                for box in r.boxes:
                    cls = int(box.cls[0])           # class index
                    conf = float(box.conf[0])       # confidence
                    xyxy = box.xyxy[0].tolist()

                    print("🟥 감지된 class idx:", cls)
                    print("🟧 감지된 class name:", names.get(cls, "unknown"))

                    detections.append({
                        "name": names.get(cls, "unknown"),
                        "confidence": round(conf, 4),
                        "bbox": xyxy
                    })
        except Exception as e:
            print("🟥 박스 파싱 중 오류:", e)
            return Response({
                "success": False,
                "error": "YOLO 결과 파싱 실패"
            }, status=500)

        # ------------------------------------------------------------
        # 5) 감지 없음 처리 (응답 반드시 전송!)
        # ------------------------------------------------------------
        if len(detections) == 0:
            print("⚠ 감지된 음식 없음")
            return Response({
                "success": True,
                "count": 0,
                "foods": []
            }, status=200)

        # ------------------------------------------------------------
        # 6) 감지 성공 응답
        # ------------------------------------------------------------
        return Response({
            "success": True,
            "count": len(detections),
            "foods": detections
        }, status=200)
