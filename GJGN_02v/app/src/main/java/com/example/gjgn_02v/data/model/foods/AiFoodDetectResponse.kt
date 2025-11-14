package com.example.gjgn_02v.data.model.foods

/**
 * AI 음식 탐지 응답 데이터 모델
 * Django API: /api/ai/food-detect/
 *
 * 서버에서 반환하는 예시(JSON):
 * {
 *   "success": true,
 *   "foods": [
 *       {
 *         "name": "김치찌개",
 *         "calorie": 180,
 *         "carb": 12.5,
 *         "protein": 8.5,
 *         "fat": 9.5,
 *         "confidence": 0.92,
 *         "bbox": [34, 120, 420, 380]
 *       }
 *   ]
 * }
 */

data class AiFoodDetectResponse(
    val success: Boolean,
    val foods: List<DetectedFoodItem>
)

data class DetectedFoodItem(
    val name: String,
    val calorie: Float,
    val carb: Float,
    val protein: Float,
    val fat: Float,
    val confidence: Float,
    val bbox: List<Float> // [x1, y1, x2, y2]
)
