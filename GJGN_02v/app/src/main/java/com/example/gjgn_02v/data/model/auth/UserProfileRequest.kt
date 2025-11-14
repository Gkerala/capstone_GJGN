package com.example.gjgn_02v.data.model.auth

data class UserProfileRequest(
    val name: String,
    val birth: String,          // yyyy-MM-dd 형태
    val gender: String,         // male / female
    val height: Int,
    val weight: Int,
    val activity_level: String  // low / medium / high
)
