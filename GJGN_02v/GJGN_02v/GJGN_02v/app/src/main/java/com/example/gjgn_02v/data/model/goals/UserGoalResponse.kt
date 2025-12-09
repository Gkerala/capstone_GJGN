package com.example.gjgn_02v.data.model.goals

data class UserGoalResponse(
    val goal_type: Int,
    val goal_weight: Float,
    val activity_level: Int,

    val kcal: Float,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val sugar: Float        // ← 추가됨
)
