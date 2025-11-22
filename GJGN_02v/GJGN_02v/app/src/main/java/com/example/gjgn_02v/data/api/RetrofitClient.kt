package com.example.gjgn_02v.data.api

import android.util.Log
import com.example.gjgn_02v.App
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // -------------------------------
    // 1) 기본 HTTP BODY 로거
    // -------------------------------
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // -------------------------------
    // 2) POST Body 강제 출력 로거
    // -------------------------------
    private val bodyLogger = Interceptor { chain ->
        val request = chain.request()

        try {
            val buffer = Buffer()
            request.body?.writeTo(buffer)
            val bodyString = buffer.readUtf8()

            Log.d("HTTP_BODY", "🔥 Request Body = $bodyString")
        } catch (e: Exception) {
            Log.e("HTTP_BODY", "🔥 Body Logging Error: ${e.message}")
        }

        chain.proceed(request)
    }

    // -------------------------------
    // 3) OkHttpClient
    // -------------------------------
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)              // 기본 HTTP 로깅
        .addInterceptor(bodyLogger)           // 🔥 JSON body 출력
        .addInterceptor(AuthInterceptor(App.context)) // 인증 헤더 붙이는 interceptor
        .build()

    // -------------------------------
    // 4) Retrofit
    // -------------------------------
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")   // Android Emulator → PC 로컬 서버
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // -------------------------------
    // 5) API 서비스
    // -------------------------------
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
