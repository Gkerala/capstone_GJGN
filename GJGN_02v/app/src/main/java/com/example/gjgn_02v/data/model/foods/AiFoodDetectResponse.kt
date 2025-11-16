package com.example.gjgn_02v.data.model.foods

/**
 * AI 음식 탐지 응답 데이터 모델
 * Django API: /api/ai/food-detect/
 *
 * 서버 JSON 예시:
 * {
 *   "success": true,
 *   "count": 2,
 *   "foods": [
 *       { "name": "Pizza", "confidence": 0.92, "bbox": [...] },
 *       { "name": "Fries", "confidence": 0.88, "bbox": [...] }
 *   ]
 * }
 */

data class AiFoodDetectResponse(
    val success: Boolean,
    val count: Int,
    val foods: List<DetectedFoodItem>
)

data class DetectedFoodItem(
    val name: String,
    val confidence: Float,
    val bbox: List<Float>        // [x1, y1, x2, y2]
)
