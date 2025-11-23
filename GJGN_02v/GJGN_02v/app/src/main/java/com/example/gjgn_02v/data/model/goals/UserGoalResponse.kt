package com.example.gjgn_02v.data.model.goals

data class UserGoalResponse(
    val goal_type: Int,
    val goal_weight: Float,
    val activity_level: Int,

    val target_kcal: Float,
    val target_carb: Float,
    val target_protein: Float,
    val target_fat: Float,
    val target_sugar: Float        // ← 추가됨
)
