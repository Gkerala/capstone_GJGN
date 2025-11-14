package com.example.gjgn_02v.data.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ---------------------------------------------------------
    // 🔐 AUTH / USER
    // ---------------------------------------------------------

    // 로그인 (카카오 토큰 → JWT)
    @POST("api/users/login/")
    fun loginWithKakao(@Body request: LoginRequest): Call<LoginResponse>

    // 내 프로필 조회
    @GET("api/users/me/")
    fun getMyProfile(): Call<UserProfileResponse>

    // 프로필 생성 / 수정
    @POST("api/users/")
    fun createOrUpdateProfile(@Body request: UserProfileRequest): Call<UserProfileResponse>

    // 회원 탈퇴
    @DELETE("api/users/")
    fun deleteUser(): Call<BasicResponse>

    // 로그아웃
    @POST("api/users/logout/")
    fun logout(): Call<BasicResponse>


    // ---------------------------------------------------------
    // 🎯 GOALS (목표)
    // ---------------------------------------------------------

    // 자동 생성 (초기 프로필 기반 목표 계산)
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


    // ---------------------------------------------------------
    // 🍱 FOODS (검색)
    // ---------------------------------------------------------

    @GET("api/foods/search/")
    fun searchFoods(
        @Query("q") query: String
    ): Call<List<FoodItemResponse>>


    // ---------------------------------------------------------
    // 🍽 RECORDS (식단 기록)
    // ---------------------------------------------------------

    // 식단 저장
    @POST("api/records/")
    fun createRecord(@Body request: MealRecordRequest): Call<MealRecordResponse>

    // 오늘 기록 조회
    @GET("api/records/today/")
    fun getTodayRecords(): Call<List<MealRecordResponse>>

    // 특정 날짜 기록 조회
    @GET("api/records/date/")
    fun getRecordsByDate(
        @Query("date") date: String // "2025-01-01" 형식
    ): Call<List<MealRecordResponse>>


    // ---------------------------------------------------------
    // 🤖 AI (이미지 음식 인식)
    // ---------------------------------------------------------

    @Multipart
    @POST("api/ai/food-detect/")
    fun detectFood(
        @Part image: MultipartBody.Part
    ): Call<AiFoodDetectResponse>
}
