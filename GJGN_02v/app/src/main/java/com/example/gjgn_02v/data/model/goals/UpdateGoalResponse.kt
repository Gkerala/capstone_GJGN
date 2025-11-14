package com.example.gjgn_02v.data.model.goals

data class UpdateGoalResponse(
    val success: Boolean,
    val message: String,
    val updated_goal: GoalResponse
)