package com.example.gjgn_02v

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        lateinit var context: android.content.Context
            private set
    }
}
