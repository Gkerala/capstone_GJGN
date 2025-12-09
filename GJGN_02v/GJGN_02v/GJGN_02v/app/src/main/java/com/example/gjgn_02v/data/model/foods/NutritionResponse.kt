package com.example.gjgn_02v.data.model.foods

data class NutritionResponse(
    val success: Boolean? = null,
    val name: String? = null,

    val calories: Float? = null,
    var grams: Float? = null,

    val carbs: Float? = null,
    val protein: Float? = null,
    val fat: Float? = null,
    val sugar: Float? = null,

    val serving_size: String? = null,
    var selected: Boolean = false
)
