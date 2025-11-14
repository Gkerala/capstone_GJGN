package com.example.gjgn_02v.data.model.foods

data class FoodSearchResponse(
    val id: Int,
    val name: String,
    val kcal: Float,
    val carb: Float,
    val protein: Float,
    val fat: Float
)