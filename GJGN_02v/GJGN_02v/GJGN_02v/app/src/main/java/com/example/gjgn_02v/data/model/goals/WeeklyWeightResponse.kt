package com.example.gjgn_02v.data.model.goals

data class WeeklyWeightResponse(
    val period: String?,
    val records: List<WeightItem>?
)

data class WeightItem(
    val date: String,
    val weight: Float?
)
