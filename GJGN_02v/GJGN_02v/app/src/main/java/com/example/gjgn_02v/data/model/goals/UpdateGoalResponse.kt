package com.example.gjgn_02v.data.model.goals

data class UpdateGoalResponse(
    val success: Boolean,
    val message: String,

    // 목표 업데이트 후 최신 goal 값
    val goal_type: Int,
    val goal_weight: Float,
    val activity_level: Int,

    // 계산된 칼로리 정보
    val kcal: Int,
    val carb: Int,
    val protein: Int,
    val fat: Int
)
