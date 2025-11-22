package com.example.gjgn_02v.data.model.goals

data class UpdateGoalRequest(
    val goal_type: Int? = null,
    val goal_weight: Float? = null,
    val activity_level: Int? = null,
    val kcal: Int? = null,
    val carb: Int? = null,
    val protein: Int? = null,
    val fat: Int? = null
)

