package com.example.gjgn_02v.data.model.auth

data class KakaoLoginResponse(
    val user: KakaoUser,
    val access: String,
    val refresh: String?,
    val is_new_user: Boolean
)

data class KakaoUser(
    val id: Int,
    val username: String,
    val email: String?,
    val profile_image: String?,
    val kakao_id: String
)