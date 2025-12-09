package com.example.gjgn_02v.data.model.foods

data class FoodSearchResponse(
    val count: Int,
    val results: List<FoodItemResponse>
)
