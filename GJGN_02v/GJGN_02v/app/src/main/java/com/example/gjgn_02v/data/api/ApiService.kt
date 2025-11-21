package com.example.gjgn_02v.data.api

import com.example.gjgn_02v.data.model.analysis.WeeklyAnalysisResponse
import com.example.gjgn_02v.data.model.auth.*
import com.example.gjgn_02v.data.model.common.BaseResponse
import com.example.gjgn_02v.data.model.foods.*
import com.example.gjgn_02v.data.model.goals.*
import com.example.gjgn_02v.data.model.home.HomeStatisticsResponse
import com.example.gjgn_02v.data.model.records.MealRecordRequest
import com.example.gjgn_02v.data.model.records.MealRecordResponse
import com.example.gjgn_02v.profile.FullProfileRequest
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

    @PUT("api/users/me/profile/")
    fun updateFullProfile(
        @Body request: FullProfileRequest
    ): Call<UserProfileResponse>


    // Logout / Delete User (Token)
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
    // 🍽️ Records
    // -------------------------------------------------------------
    @POST("api/records/")
    fun createRecord(
        @Body request: MealRecordRequest
    ): Call<MealRecordResponse>

    @POST("api/records/")
    suspend fun createRecordRaw(
        @Body req: Map<String, String>
    ): Response<Any>

    @GET("api/records/today/stat/")
    fun getTodayRecords(): Call<List<MealRecordResponse>>

    @GET("api/records/date/")
    fun getRecordsByDate(
        @Query("date") date: String
    ): Call<List<MealRecordResponse>>


    // -------------------------------------------------------------
    // 🤖 YOLO AI 분석
    // -------------------------------------------------------------
    @Multipart
    @POST("api/ai/food-detect/")
    fun detectFood(
        @Part image: MultipartBody.Part
    ): Call<AiFoodDetectResponse>


    // -------------------------------------------------------------
    // 🥗 Nutrition (최종)
    // -------------------------------------------------------------
    @GET("api/foods/nutrition/")
    suspend fun getNutrition(
        @Query("name") foodName: String
    ): Response<NutritionResponse>


    // -------------------------------------------------------------
    // 📊 분석 API
    // -------------------------------------------------------------
    @GET("api/records/analysis/weekly/")
    fun getWeeklyAnalysis(): Call<WeeklyAnalysisResponse>

    @GET("api/records/week/stat/")
    fun getWeeklyAchievement(): Call<GoalStatResponse>

    @GET("api/records/analysis/monthly/")
    fun getMonthlyAchievement(): Call<GoalStatResponse>


    // -------------------------------------------------------------
    // 🏠 홈 통계
    // -------------------------------------------------------------
    @GET("api/home/statistics/")
    fun getHomeStatistics(): Call<HomeStatisticsResponse>


    // -------------------------------------------------------------
    // ⚖️ 체중 API
    // -------------------------------------------------------------
    @GET("api/goals/weights/weekly/")
    fun getWeeklyWeight(): Call<WeeklyWeightResponse>

    @POST("api/goals/weights/")
    fun createWeight(
        @Body weightRequest: WeightRequest
    ): Call<WeightResponse>

}
