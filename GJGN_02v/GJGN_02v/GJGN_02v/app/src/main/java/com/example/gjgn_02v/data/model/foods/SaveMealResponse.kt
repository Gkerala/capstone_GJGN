package com.example.gjgn_02v.data.model.foods

data class SaveMealResponse(
    val id: Int,
    val date: String,
    val total_kcal: Float,
    val created_at: String
)