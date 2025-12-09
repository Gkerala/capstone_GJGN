package com.example.gjgn_02v.data.model.records

data class MealItem(
    val id: Int,
    val name: String,
    val kcal: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double
)