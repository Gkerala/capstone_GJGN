package com.example.gjgn_02v.data.model.records

data class MealRecordResponse(
    val id: Int,
    val food_id: Int,
    val meal_type: String,
    val calories: Int,
    val created_at: String
)
