package com.example.gjgn_02v.data.model.records

data class MealDayResponse(
    val breakfast: List<MealItem>,
    val lunch: List<MealItem>,
    val dinner: List<MealItem>
)