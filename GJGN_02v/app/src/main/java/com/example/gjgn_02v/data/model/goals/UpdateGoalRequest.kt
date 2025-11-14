package com.example.gjgn_02v.data.model.goals

data class UpdateGoalRequest(
    val kcal: Int,
    val carb: Int,
    val protein: Int,
    val fat: Int
)