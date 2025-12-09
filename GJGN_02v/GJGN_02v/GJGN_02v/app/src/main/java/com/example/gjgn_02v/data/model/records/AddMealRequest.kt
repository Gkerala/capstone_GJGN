package com.example.gjgn_02v.data.model.records

data class AddMealRequest(
    val name: String,
    val kcal: Int,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
    val mealType: String   // "breakfast", "lunch", "dinner"
)