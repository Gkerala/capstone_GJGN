package com.example.gjgn_02v.data.model.analysis

data class WeeklyAnalysisResponse(
    val weekly_records: List<DayRecord>?,
    val weights: List<DayWeight>?
)

data class DayRecord(
    val date: String,
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double
)

data class DayWeight(
    val date: String,
    val weight: Double
)
