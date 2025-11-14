package com.example.gjgn_02v.data.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    // -----------------------------
    // 🔐 1) Auth & User
    // -----------------------------

    // 카카오 로그인 후 JWT 발급
    @POST("api/users/login/")
    fun loginWithKakao(@Body request: LoginRequest): Call<LoginResponse>

    // 내 프로필 조회
    @GET("api/users/me/")
    fun getMyProfile(): Call<UserProfileResponse>

    // 프로필 생성/수정
    @POST("api/users/")
    fun createOrUpdateProfile(@Body request: UserProfileRequest): Call<UserProfileResponse>

    // 회원 탈퇴
    @DELETE("api/users/")
    fun deleteUser(): Call<BasicResponse>

    // 로그아웃 (서버 세션 제거 방식)
    @POST("api/users/logout/")
    fun logout(): Call<BasicResponse>


    // -----------------------------
    // 🎯 2) Goals (목표)
    // -----------------------------

    // 자동 생성
    @POST("api/goals/auto/")
    fun autoGenerateGoal(@Body request: AutoGoalRequest): Call<GoalResponse>

    // 조회
    @GET("api/goals/")
    fun getGoal(): Call<GoalResponse>

    // 수정
    @PATCH("api/goals/")
    fun updateGoal(@Body request: GoalUpdateRequest): Call<GoalResponse>

    // 주간 통계
    @GET("api/goals/weekly/")
    fun getWeeklyAchievement(): Call<GoalStatResponse>

    // 월간 통계
    @GET("api/goals/monthly/")
    fun getMonthlyAchievement(): Call<GoalStatResponse>


    // -----------------------------
    // 🍱 3) Foods (음식 검색)
    // -----------------------------

    @GET("api/foods/search/")
    fun searchFoods(@Query("q") query: String): Call<List<FoodItemResponse>>


    // -----------------------------
    // 🍽 4) Records (식단 저장)
    // -----------------------------

    // 식단 저장
    @POST("api/records/")
    fun createRecord(@Body request: MealRecordRequest): Call<MealRecordResponse>

    // 오늘의 기록 조회
    @GET("api/records/today/")
    fun getTodayRecords(): Call<List<MealRecordResponse>>

    // 특정 날짜 기록 조회
    @GET("api/records/date/")
    fun getRecordsByDate(@Query("date") date: String): Call<List<MealRecordResponse>>


    // -----------------------------
    // 🤖 5) AI (이미지 업로드)
    // -----------------------------

    @Multipart
    @POST("api/ai/food-detect/")
    fun detectFood(
        @Part image: MultipartBody.Part
    ): Call<AiFoodDetectResponse>
}