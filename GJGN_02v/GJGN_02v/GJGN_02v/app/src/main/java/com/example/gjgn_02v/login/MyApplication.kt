package com.example.gjgn_02v.login

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "15717b2f42caeea1ee8e0d45226b3236")
    }
}

