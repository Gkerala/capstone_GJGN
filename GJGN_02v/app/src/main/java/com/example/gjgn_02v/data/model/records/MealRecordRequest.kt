package com.example.gjgn_02v.data.model.records


data class MealRecordRequest(
    val food_id: Int,
    val amount: Int = 1,
    val meal_type: String = "breakfast"   // breakfast / lunch / dinner
)
