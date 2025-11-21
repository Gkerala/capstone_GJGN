package com.example.gjgn_02v.data.model.foods

data class NutritionResponse(
    val success: Boolean,
    val name: String?,
    val calories: Float?,
    val carbs: Float?,
    val protein: Float?,
    val fat: Float?,
    val sugar: Float?,
    val serving_size: String?,
    var selected: Boolean = false
)