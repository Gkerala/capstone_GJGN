package com.example.gjgn_02v

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        // ⭐ Kakao SDK 초기화 (앱 실행 시 1회만 실행됨)
        KakaoSdk.init(this, "15717b2f42caeea1ee8e0d45226b3236")
    }

    companion object {
        lateinit var context: android.content.Context
            private set
    }
}
