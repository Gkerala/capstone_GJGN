package com.example.gjgn_02v.data.api

import com.example.gjgn_02v.data.model.auth.KakaoLoginRequest
import com.example.gjgn_02v.data.model.auth.KakaoLoginResponse
import com.example.gjgn_02v.data.model.auth.UserProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import com.example.gjgn_02v.data.model.auth.DeleteUserResponse

import com.example.gjgn_02v.data.model.common.BaseResponse

import com.example.gjgn_02v.data.model.foods.FoodSearchResponse
import com.example.gjgn_02v.data.model.foods.SaveMealRequest
import com.example.gjgn_02v.data.model.foods.SaveMealResponse
import com.example.gjgn_02v.data.model.foods.AiFoodDetectResponse
import com.example.gjgn_02v.data.model.foods.FoodItemResponse

import com.example.gjgn_02v.data.model.goals.GoalResponse
import com.example.gjgn_02v.data.model.goals.GoalStatResponse
import com.example.gjgn_02v.data.model.goals.UpdateGoalRequest
import com.example.gjgn_02v.data.model.goals.UpdateGoalResponse
import com.example.gjgn_02v.data.model.goals.WeeklyWeightResponse

import com.example.gjgn_02v.data.model.home.HomeStatisticsResponse

import com.example.gjgn_02v.data.model.records.MealRecordRequest
import com.example.gjgn_02v.data.model.records.MealRecordResponse

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // -------------------------------------------------------------
    // 🔐 1) Auth & User
    // -------------------------------------------------------------

    @POST("api/users/login/")
    fun loginWithKakao(
        @Body request: KakaoLoginRequest
    ): Call<KakaoLoginResponse>

    @GET("api/users/me/")
    fun getMyProfile(): Call<UserProfileResponse>

    @PUT("api/users/me/")
    fun updateMyProfile(
        @Body request: UserProfileRequest
    ): Call<UserProfileResponse>

    @DELETE("api/users/")
    fun deleteUser(): Call<DeleteUserResponse>

    @POST("api/users/logout/")
    fun logout(): Call<BaseResponse>


    // -------------------------------------------------------------
    // 🎯 2) Goals
    // -------------------------------------------------------------

    @GET("api/goals/")
    fun getGoal(): Call<GoalResponse>

    @PATCH("api/goals/")
    fun updateGoal(
        @Body request: UpdateGoalRequest
    ): Call<UpdateGoalResponse>


    // -------------------------------------------------------------
    // 🍱 3) Foods
    // -------------------------------------------------------------

    @GET("api/foods/search/")
    fun searchFoods(
        @Query("q") query: String
    ): Call<List<FoodItemResponse>>

    @POST("api/foods/save/")
    fun saveMeal(
        @Body request: SaveMealRequest
    ): Call<SaveMealResponse>


    // -------------------------------------------------------------
    // 🍽 4) Records (식단 기록)
    // -------------------------------------------------------------

    @POST("api/records/")
    fun createRecord(
        @Body request: MealRecordRequest
    ): Call<MealRecordResponse>

    @GET("api/records/today/")
    fun getTodayRecords(): Call<List<MealRecordResponse>>

    @GET("api/records/date/")
    fun getRecordsByDate(
        @Query("date") date: String
    ): Call<List<MealRecordResponse>>


    // -------------------------------------------------------------
    // 🤖 5) AI (YOLO 음식 검출)
    // -------------------------------------------------------------

    @Multipart
    @POST("api/ai/food-detect/")
    fun detectFood(
        @Part image: MultipartBody.Part
    ): Call<AiFoodDetectResponse>


    // -------------------------------------------------------------
    // 📊 6) Home 통계
    // -------------------------------------------------------------

    @GET("api/home/statistics/")
    fun getHomeStatistics(): Call<HomeStatisticsResponse>

    // 현재 로그인 사용자 정보 조회
    @GET("api/users/me/")
    fun getCurrentUser(
        @Header("Authorization") token: String
    ): Call<UserProfileResponse>

    @GET("api/analysis/weekly/")
    fun getWeeklyAchievement(): Call<GoalStatResponse>

    @GET("api/analysis/monthly/")
    fun getMonthlyAchievement(): Call<GoalStatResponse>

    @POST("api/logout/")
    fun logoutUser(
        @Header("Authorization") auth: String,
        @Body refresh: Map<String, String>
    ): Call<Void>

    @HTTP(method = "DELETE", path = "api/user/delete/", hasBody = true)
    fun deleteUser(
        @Header("Authorization") auth: String,
        @Body refresh: Map<String, String>
    ): Call<Void>

    @GET("goals/weights/weekly/")
    fun getWeeklyWeight(): Call<WeeklyWeightResponse>
}
