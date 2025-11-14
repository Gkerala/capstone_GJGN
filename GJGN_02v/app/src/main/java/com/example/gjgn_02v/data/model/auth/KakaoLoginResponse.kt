package com.example.gjgn_02v.data.model.auth

data class KakaoLoginResponse(
    val access: String,
    val refresh: String? = null,
    val is_new_user: Boolean
)