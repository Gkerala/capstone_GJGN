package com.example.gjgn_02v.data.api

import android.content.Context
import com.example.gjgn_02v.data.api.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val token = TokenManager.getAccessToken(context)

        println("🔥 AuthInterceptor - Loaded Token: $token")
        println("🚀 Request URL: ${chain.request().url}")
        println("🚀 Request Method: ${chain.request().method}")

        val newRequest = chain.request().newBuilder().apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            } else {
                println("🚨 AuthInterceptor - Token is NULL or EMPTY")
            }
        }.build()

        println("📡 Final Authorization Header: ${newRequest.header("Authorization")}")

        return chain.proceed(newRequest)
    }
}


