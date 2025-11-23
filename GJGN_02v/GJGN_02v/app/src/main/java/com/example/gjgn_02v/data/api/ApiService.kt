package com.example.gjgn_02v.data.api

import com.example.gjgn_02v.data.model.analysis.WeeklyAnalysisResponse
import com.example.gjgn_02v.data.model.auth.*
import com.example.gjgn_02v.data.model.common.BaseResponse
import com.example.gjgn_02v.data.model.foods.*
import com.example.gjgn_02v.data.model.goals.*
import com.example.gjgn_02v.data.model.analysis.MainSummaryResponse
import com.example.gjgn_02v.data.model.records.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // -------------------------------------------------------------
    // 🔐 Auth
    // -------------------------------------------------------------
    @POST("api/auth/login/kakao/")
    fun loginWithKakao(@Body request: KakaoLoginRequest): Call<KakaoLoginResponse>

    @GET("api/users/me/")
    fun getMyProfile(): Call<UserProfileResponse>

    @PUT("api/users/me/goal/update/")
    fun updateFullProfile(@Body request: FullProfileRequest): Call<UserProfileResponse>

    @GET("api/goals/me/")
    fun getGoal(): Call<UserGoalResponse>

    @PATCH("api/users/me/")
    fun updateMyProfile(@Body request: UserProfileRequest): Call<UserProfileResponse>

    @PATCH("api/goals/update/")
    fun updateGoal(@Body request: UpdateGoalRequest): Call<UpdateGoalResponse>

    @DELETE("api/users/delete/")
    fun deleteUser(): Call<Void>


    // -------------------------------------------------------------
    // 🍱 Foods
    // -------------------------------------------------------------
    @GET("api/foods/search/")
    fun searchFoods(@Query("q") query: String): Call<FoodSearchResponse>

    @POST("api/foods/save/")
    fun saveMeal(@Body request: SaveMealRequest): Call<SaveMealResponse>

    @GET("api/foods/nutrition/")
    suspend fun getNutrition(@Query("name") name: String): Response<NutritionResponse>


    // -------------------------------------------------------------
    // 🍽️ Records
    // -------------------------------------------------------------
    @POST("api/records/create/")
    fun createRecord(@Body body: MealRecordRequest): Call<MealRecordResponse>


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
    fun getWeeklyAnalysis(
        @Query("date") date: String
    ): Call<WeeklyAnalysisResponse>

    @GET("api/records/analysis/weights/weekly/")
    fun getWeeklyWeight(
        @Query("date") date: String
    ): Call<WeeklyWeightResponse>

    @GET("api/records/today/stat/")
    fun getTodayRecords(): Call<TodayStatResponse>


    // -------------------------------------------------------------
    // ⚖️ 체중 (단독 조회/생성)
    // -------------------------------------------------------------
    @POST("api/records/weights/create/")
    fun createWeight(@Body req: WeightRequest): Call<WeightResponse>

    @GET("api/records/weights/")
    fun getWeights(): Call<List<WeightResponse>>

    @GET("/api/records/main/summary/")
    fun getMainSummary(): Call<MainSummaryResponse>

}
