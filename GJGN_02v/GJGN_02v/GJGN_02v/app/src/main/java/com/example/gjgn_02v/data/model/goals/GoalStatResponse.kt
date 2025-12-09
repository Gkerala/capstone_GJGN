package com.example.gjgn_02v.data.model.goals

data class GoalStatResponse(
    val achievement: Int,          // ← 주간/월간 달성률
    val weekly: List<WeeklyCalorieItem>? = null,
    val monthly: List<MonthlyCalorieItem>? = null
)

data class WeeklyCalorieItem(
    val date: String,
    val calories: Int
)

data class MonthlyCalorieItem(
    val date: String,
    val calories: Int
)
