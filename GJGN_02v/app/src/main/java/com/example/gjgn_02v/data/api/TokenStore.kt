package com.example.gjgn_02v.data.api

import android.content.Context

object TokenStore {
    private const val PREFS = "prefs_token"
    private const val KEY_ACCESS = "access_token"

    var cachedToken: String? = null
        private set

    fun saveToken(context: Context, token: String) {
        cachedToken = token
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACCESS, token).apply()
    }

    fun loadToken(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        cachedToken = prefs.getString(KEY_ACCESS, null)
    }

    fun clear(context: Context) {
        cachedToken = null
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_ACCESS).apply()
    }
}
