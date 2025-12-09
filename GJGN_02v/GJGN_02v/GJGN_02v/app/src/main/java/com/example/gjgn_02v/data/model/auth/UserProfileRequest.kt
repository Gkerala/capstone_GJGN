package com.example.gjgn_02v.data.model.auth

data class UserProfileRequest(
    val name: String? = null,
    val birth: String? = null,
    val gender: String? = null,
    val height: Float? = null,
    val weight: Float? = null
)

