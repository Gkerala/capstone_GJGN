package com.example.gjgn_02v.data.model.records

data class MealRecordRequest(
    val meal_type: String,
    val foods: List<FoodData>
)

data class FoodData(
    val food_name: String,
    val amount: Float,
    val kcal: Float,
    val carb: Float,
    val protein: Float,
    val fat: Float,
    val sugar: Float
)
