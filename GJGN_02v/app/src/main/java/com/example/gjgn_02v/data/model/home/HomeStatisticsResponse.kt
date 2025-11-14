package com.example.gjgn_02v.data.model.home

data class HomeStatisticsResponse(
    val daily_kcal: Int,
    val weekly_avg_kcal: Int,
    val achieve_rate: Float,
    val top_foods: List<TopFood>
)

data class TopFood(
    val name: String,
    val count: Int
)