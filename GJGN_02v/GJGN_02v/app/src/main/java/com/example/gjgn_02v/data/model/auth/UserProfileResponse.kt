package com.example.gjgn_02v.data.model.auth

data class UserProfileResponse(
    val id: Int,

    // 기본 정보
    val name: String,
    val email: String?,
    val birth: String?,
    val gender: String,
    val height: Float,
    val weight: Float,

    // 목표 정보
    val goal_type: Int,
    val goal_weight: Float,

    // 활동량(1~5)
    val activity_level: Int,

    // 칼로리 결과
    val kcal: Int,
    val carb: Int,
    val protein: Int,
    val fat: Int,

    val profile_completed: Boolean,
    val created_at: String
)

