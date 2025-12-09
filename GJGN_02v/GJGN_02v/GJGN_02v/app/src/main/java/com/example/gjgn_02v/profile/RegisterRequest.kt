package com.example.gjgn_02v.profile

data class RegisterRequest(
    val gender: String?,
    val birth: String?,
    val height: Float?,
    val weight: Float?,
    val goal_weight: Float?,
    val activity_level: Int?,
    val goal_type: String?
)
