package com.example.gjgn_02v.data.model.goals

data class UpdateGoalResponse(
    val success: Boolean,
    val message: String,
    val calorie: Int,
    val carb: Int,
    val protein: Int,
    val fat: Int
)