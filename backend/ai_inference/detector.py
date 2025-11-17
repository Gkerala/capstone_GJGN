# ai_inference/detector.py
from ultralytics import YOLO
import os

# backend/ai_inference/
CURRENT_DIR = os.path.dirname(__file__)

# backend/
BASE_DIR = os.path.dirname(CURRENT_DIR)

# backend/ai_inference/yolo_model/best.pt
MODEL_PATH = os.path.join(CURRENT_DIR, "yolo_model", "best.pt")

# 모델 로드
model = YOLO(MODEL_PATH)


def detect_food(image_path):
    results = model(image_path)

    detections = []

    for r in results:
        for box in r.boxes:
            cls_id = int(box.cls[0])
            name = model.names[cls_id]
            conf = float(box.conf[0])
            x1, y1, x2, y2 = box.xyxy[0].tolist()

            detections.append({
                "name": name,
                "confidence": round(conf, 4),
                "bbox": [x1, y1, x2, y2]
            })

    return detections
