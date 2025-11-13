package com.example.gjgn_02v

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "auth_pref"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    fun saveTokens(context: Context, access: String, refresh: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACCESS, access).putString(KEY_REFRESH, refresh).apply()
    }

    fun getAccessToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCESS, null)
    }

    fun getRefreshToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REFRESH, null)
    }

    fun clearTokens(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    // ✅ 추가 (MyPageActivity와 호환용)
    fun getToken(context: Context): String? = getAccessToken(context)
    fun clearToken(context: Context) = clearTokens(context)
}
