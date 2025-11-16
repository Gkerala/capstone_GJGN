package com.example.gjgn_02v.data.model.auth

data class UserProfileResponse(
    val id: Int,
    val email: String,
    val nickname: String,
    val age: Int,
    val gender: String,
    val height: Float,
    val weight: Float,
    val activity_level: String,
    val profile_completed: Boolean,
    val created_at: String
)
