package com.example.gjgn_02v.data.api

import com.example.gjgn_02v.data.model.auth.*
import com.example.gjgn_02v.data.model.common.BaseResponse
import com.example.gjgn_02v.data.model.foods.*
import com.example.gjgn_02v.data.model.goals.*
import com.example.gjgn_02v.data.model.home.HomeStatisticsResponse
import com.example.gjgn_02v.data.model.records.MealRecordRequest
import com.example.gjgn_02v.data.model.records.MealRecordResponse

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // -------------------------------------------------------------
    // 🔐 Auth
    // -------------------------------------------------------------
    @POST("api/auth/login/kakao/")
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
    // Logout / Delete User with Token
    // -------------------------------------------------------------
    @POST("api/users/logout/")
    fun logoutUser(
        @Header("Authorization") auth: String,
        @Body refresh: Map<String, String>
    ): Call<Void>

    @HTTP(method = "DELETE", path = "api/users/delete/", hasBody = true)
    fun deleteUser(
        @Header("Authorization") auth: String,
        @Body refresh: Map<String, String>
    ): Call<Void>


    // -------------------------------------------------------------
    // 🎯 Goals
    // -------------------------------------------------------------
    @GET("api/goals/")
    fun getGoal(): Call<GoalResponse>

    @PATCH("api/goals/")
    fun updateGoal(
        @Body request: UpdateGoalRequest
    ): Call<UpdateGoalResponse>


    // -------------------------------------------------------------
    // 🍱 Foods
    // -------------------------------------------------------------
    @GET("api/foods/search/")
    fun searchFoods(
        @Query("q") query: String
    ): Call<FoodSearchResponse>



    @POST("api/foods/save/")
    fun saveMeal(
        @Body request: SaveMealRequest
    ): Call<SaveMealResponse>


    // -------------------------------------------------------------
    // 🍽 Records
    // -------------------------------------------------------------
    @POST("api/records/")
    fun createRecord(
        @Body request: MealRecordRequest
    ): Call<MealRecordResponse>

    // 🔥 여러개 저장용 / Raw Map 저장
    @POST("api/records/")
    suspend fun createRecordRaw(
        @Body req: Map<String, String>
    ): Response<Any>

    @GET("api/records/today/")
    fun getTodayRecords(): Call<List<MealRecordResponse>>

    @GET("api/records/date/")
    fun getRecordsByDate(
        @Query("date") date: String
    ): Call<List<MealRecordResponse>>


    // -------------------------------------------------------------
    // 🤖 AI YOLO Food Detect
    // -------------------------------------------------------------
    @Multipart
    @POST("api/ai/food-detect/")
    fun detectFood(
        @Part image: MultipartBody.Part
    ): Call<AiFoodDetectResponse>


    // -------------------------------------------------------------
    // 🥗 Nutrition API (최종 사용)
    // -------------------------------------------------------------
    @GET("api/foods/nutrition/")
    suspend fun getNutrition(
        @Query("name") foodName: String
    ): Response<NutritionResponse>


    // -------------------------------------------------------------
    // 📊 분석 API
    // -------------------------------------------------------------
    @GET("api/records/analysis/weekly/")
    fun getWeeklyAchievement(): Call<GoalStatResponse>

    @GET("api/records/analysis/monthly/")
    fun getMonthlyAchievement(): Call<GoalStatResponse>


    // -------------------------------------------------------------
    // 홈 통계
    // -------------------------------------------------------------
    @GET("api/home/statistics/")
    fun getHomeStatistics(): Call<HomeStatisticsResponse>

    @GET("goals/weights/weekly/")
    fun getWeeklyWeight(): Call<WeeklyWeightResponse>
}
