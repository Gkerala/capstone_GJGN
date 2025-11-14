package com.example.gjgn_02v.data.model.goals

data class WeeklyWeightResponse(
    val weekly_weight: List<WeightItem>
)

data class WeightItem(
    val date: String,
    val weight: Float?
)
