package com.example.gjgn_02v.data.model.foods

data class SaveMealRequest(
    val date: String,
    val items: List<MealItem>
)

data class MealItem(
    val amount: Float
)