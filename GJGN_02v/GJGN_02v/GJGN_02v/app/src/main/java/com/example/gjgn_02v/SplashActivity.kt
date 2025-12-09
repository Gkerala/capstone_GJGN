package com.example.gjgn_02v

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.gjgn_02v.login.LoginActivity
import com.example.gjgn_02v.main.MainActivity
import com.example.gjgn_02v.data.api.TokenManager

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accessToken = TokenManager.getAccessToken(this)

        if (accessToken.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }

        finish()
    }
}
