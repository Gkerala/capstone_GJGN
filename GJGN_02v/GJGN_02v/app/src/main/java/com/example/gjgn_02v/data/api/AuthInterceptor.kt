package com.example.gjgn_02v.data.api

import android.content.Context
import com.example.gjgn_02v.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val token = TokenManager.getAccessToken(context)

        println("🔥 AuthInterceptor - Loaded Token: $token")

        val request = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            println("🚨 AuthInterceptor - Token is NULL or EMPTY")
            chain.request()
        }

        println("📡 Final Authorization Header: ${request.header("Authorization")}")

        return chain.proceed(request)
    }
}

