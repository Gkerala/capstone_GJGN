package com.example.gjgn_02v

// 서버로부터 JWT + 사용자 응답
data class TokenResponse(
    val access: String,
    val refresh: String,
    val user: UserProfile? = null // ✅ 사용자 정보 포함
)

// 카카오 로그인 교환 요청
data class KakaoTokenExchangeRequest(
    val access_token: String
)

// 사용자 프로필 모델 (서버 UserInfoSerializer와 동일)
data class UserProfile(
    val username: String,          // Django: username
    val email: String,             // Django: email
    val birth_date: String?,       // null 가능
    val gender: String?,
    val height_cm: Float?,
    val weight_kg: Float?,
    val goal_type: String?,
    val target_weight_kg: Float?,
    val activity_level: String?,
    val profile_completed: Boolean? = null
)

// 기존 로그인 요청 (테스트용)
data class LoginRequest(
    val username: String,
    val password: String
)

// 음식 데이터 (예제용)
data class FoodDto(
    val id: Int,
    val food_name: String,
    val calories: Float
)
