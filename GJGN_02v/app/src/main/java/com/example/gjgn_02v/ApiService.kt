package com.example.gjgn_02v

import retrofit2.Call
import retrofit2.http.*

// ✅ 서버 통신용 API 정의
interface ApiService {

    // 🔹 1. 카카오 로그인 → JWT 발급
    @POST("api/auth/kakao/")
    fun loginWithKakao(@Body body: Map<String, String>): Call<TokenResponse>

    // 🔹 2. (테스트용) 일반 로그인
    @POST("api/token/")
    fun login(@Body body: LoginRequest): Call<TokenResponse>

    // 🔹 3. 단일 사용자 프로필 조회 (서버에서 JWT 기반으로 본인만 반환)
    //    /api/users/me/ ← 새로 추가된 엔드포인트
    @GET("api/users/me/")
    fun getProfile(
        @Header("Authorization") authHeader: String
    ): Call<UserProfile>

    // 🔹 4. 유저 프로필 생성/업데이트 (ProfileSetupActivity에서 사용)
    @POST("api/users/")
    fun submitProfile(
        @Header("Authorization") authHeader: String,
        @Body profile: UserProfile
    ): Call<Void>

    // 🔹 5. 음식 목록 불러오기 (참고용)
    @GET("api/foods/")
    fun getFoods(
        @Header("Authorization") authHeader: String
    ): Call<List<FoodDto>>

    @GET("/api/users/me/")
    fun getCurrentUser(
        @Header("Authorization") token: String
    ): Call<UserProfile>

    @POST("api/auth/logout/")
    fun logoutUser(
        @Header("Authorization") accessToken: String,
        @Body body: Map<String, String> // {"refresh_token": "..."}
    ): Call<Void>

    @HTTP(method = "DELETE", path = "api/auth/delete/", hasBody = true)
    fun deleteUser(
        @Header("Authorization") accessToken: String,
        @Body body: Map<String, String>
    ): Call<Void>
}
