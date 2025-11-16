package com.example.gjgn_02v.data.model.foods

data class SaveMealRequest(
    val date: String,
    val items: List<MealItem>
)

data class MealItem(
    val food_id: Int,
    val amount: Float
)