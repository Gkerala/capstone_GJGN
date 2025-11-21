package com.example.gjgn_02v.data.api

import com.example.gjgn_02v.data.model.analysis.WeeklyAnalysisResponse
import com.example.gjgn_02v.data.model.auth.*
import com.example.gjgn_02v.data.model.common.BaseResponse
import com.example.gjgn_02v.data.model.foods.*
import com.example.gjgn_02v.data.model.goals.*
import com.example.gjgn_02v.data.model.home.HomeStatisticsResponse
import com.example.gjgn_02v.data.model.records.MealRecordRequest
import com.example.gjgn_02v.data.model.records.MealRecordResponse
import com.example.gjgn_02v.data.model.auth.FullProfileRequest
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

    // ⭐ 프로필(온보딩) 전체 저장
    @PUT("api/users/me/profile/")
    fun updateFullProfile(
        @Body request: FullProfileRequest
    ): Call<UserProfileResponse>


    // -------------------------------------------------------------
    // 🔐 Delete User
    // -------------------------------------------------------------

    @DELETE("api/users/delete/")
    fun deleteUser(): Call<Void>


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
    fun searchFoods(@Query("q") query: String): Call<FoodSearchResponse>

    @POST("api/foods/save/")
    fun saveMeal(@Body request: SaveMealRequest): Call<SaveMealResponse>

    @GET("foods/nutrition/")
    suspend fun getNutrition(
        @Query("name") name: String
    ): Response<NutritionResponse>

    // -------------------------------------------------------------
    // 🍽️ Records
    // -------------------------------------------------------------
    @POST("api/records/")
    fun createRecord(@Body request: MealRecordRequest): Call<MealRecordResponse>

    @POST("api/records/")
    suspend fun createRecordRaw(@Body req: Map<String, String>): Response<Any>

    @GET("api/records/today/stat/")
    fun getTodayRecords(): Call<List<MealRecordResponse>>

    @GET("api/records/date/")
    fun getRecordsByDate(@Query("date") date: String): Call<List<MealRecordResponse>>


    // -------------------------------------------------------------
    // 🤖 YOLO AI
    // -------------------------------------------------------------
    @Multipart
    @POST("api/ai/food-detect/")
    fun detectFood(@Part image: MultipartBody.Part): Call<AiFoodDetectResponse>


    // -------------------------------------------------------------
    // 📊 분석
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
    // ⚖️ 체중
    // -------------------------------------------------------------
    @GET("api/goals/weights/weekly/")
    fun getWeeklyWeight(): Call<WeeklyWeightResponse>

    @POST("api/goals/weights/")
    fun createWeight(@Body weightRequest: WeightRequest): Call<WeightResponse>
}
