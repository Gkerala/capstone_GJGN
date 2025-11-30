package com.example.gjgn_02v.data.model.analysis

data class MainSummaryResponse(
    val today: TodayStat,
    val weight: WeightInfo,
    val meals: MealTypes
)

data class TodayStat(
    val total_kcal: Float,
    val goal_kcal: Float,
    val kcal_percent: Float,

    val carb: Float,
    val goal_carb: Float,
    val carb_percent: Float,

    val protein: Float,
    val goal_protein: Float,
    val protein_percent: Float,

    val fat: Float,
    val goal_fat: Float,
    val fat_percent: Float,

    val sugar: Float,
    val goal_sugar: Float,
    val sugar_percent: Float
)

data class WeightInfo(
    val start_weight: Float?,
    val today_weight: Float?,
    val goal_weight: Float?
)

data class MealTypes(
    val breakfast: MealInfo?,
    val lunch: MealInfo?,
    val dinner: MealInfo?
)

data class MealInfo(
    val foods: List<FoodInfo>,
    val total: FoodInfo
)

data class FoodInfo(
    val name: String,
    val kcal: Float,
    val carb: Float,
    val protein: Float,
    val fat: Float,
    val sugar: Float
)
