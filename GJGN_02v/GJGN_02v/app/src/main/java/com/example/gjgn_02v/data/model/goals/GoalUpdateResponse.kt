package com.example.gjgn_02v.data.model.goals

data class GoalUpdateResponse(
    val success: Boolean,
    val message: String?,
    val data: GoalData?
)

data class GoalData(
    val goal_type: Int?,
    val goal_weight: Float?,
    val activity_level: Int?,
    val kcal: Int?,
    val carb: Int?,
    val protein: Int?,
    val fat: Int?
)
