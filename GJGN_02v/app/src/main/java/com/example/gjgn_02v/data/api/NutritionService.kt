package com.example.gjgn_02v.data.api

import com.example.gjgn_02v.data.model.foods.NutritionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NutritionService {

    @GET("foods/nutrition/")
    suspend fun getNutrition(
        @Query("name") name: String
    ): Response<NutritionResponse>
}
