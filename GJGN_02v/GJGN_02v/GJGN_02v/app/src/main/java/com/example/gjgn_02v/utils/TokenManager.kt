package com.example.gjgn_02v.utils

import android.content.Context
import androidx.core.content.edit

object TokenManager {

    private const val PREF_NAME = "auth_pref"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    // 항상 applicationContext 사용 → Activity context 메모리 누수 방지
    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // 저장 시 KTX edit {} 사용
    fun saveTokens(context: Context, access: String, refresh: String) {
        println("💾 Saving Access Token: $access")
        println("💾 Saving Refresh Token: $refresh")

        prefs(context).edit {
            putString(KEY_ACCESS, access)
            putString(KEY_REFRESH, refresh)
        }
    }

    fun getAccessToken(context: Context): String? {
        return prefs(context).getString(KEY_ACCESS, null)
    }

    fun getRefreshToken(context: Context): String? {
        return prefs(context).getString(KEY_REFRESH, null)
    }

    fun clearTokens(context: Context) {
        prefs(context).edit {
            clear()
        }
    }
}
