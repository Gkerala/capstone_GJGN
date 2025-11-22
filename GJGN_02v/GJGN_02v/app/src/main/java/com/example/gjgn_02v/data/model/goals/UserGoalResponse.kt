package com.example.gjgn_02v.data.model.goals

data class UserGoalResponse(
    val goal_type: Int,
    val goal_weight: Float,
    val activity_level: Int,
    val target_kcal: Int,
    val target_carb: Int,
    val target_protein: Int,
    val target_fat: Int
)