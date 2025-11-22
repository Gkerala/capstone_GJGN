package com.example.gjgn_02v.data.model.records

data class MealRecordRequest(
    val food_id: Int? = null,
    val meal_type: String,
    val amount: Float = 1f,
    val name: String? = null,
    val calories: Float? = null,
    val carbs: Float? = null,
    val protein: Float? = null,
    val fat: Float? = null,
    val sugar: Float? = null
)


