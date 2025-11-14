package com.example.gjgn_02v.data.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Context를 직접 참조할 수 없으니 TokenStore의 static context 사용
        val token = TokenStore.cachedToken

        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(newRequest)
    }
}
