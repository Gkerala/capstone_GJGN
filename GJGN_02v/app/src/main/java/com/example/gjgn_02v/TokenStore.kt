package com.example.gjgn_02v

import android.content.Context

object TokenStore {
    private const val PREFS = "prefs_token"
    private const val KEY_ACCESS = "access_token"

    fun saveToken(context: Context, accessToken: String) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putString(KEY_ACCESS, accessToken).apply()
    }

    fun getToken(context: Context): String? {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.getString(KEY_ACCESS, null)
    }

    fun clear(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().remove(KEY_ACCESS).apply()
    }
}
