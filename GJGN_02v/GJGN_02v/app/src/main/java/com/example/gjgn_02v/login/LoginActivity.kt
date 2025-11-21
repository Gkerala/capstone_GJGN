package com.example.gjgn_02v.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.main.MainActivity
import com.example.gjgn_02v.profile.ProfileSetupActivity
import com.example.gjgn_02v.utils.TokenManager
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.KakaoLoginRequest
import com.example.gjgn_02v.data.model.auth.KakaoLoginResponse
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        KakaoSdk.init(this, "15717b2f42caeea1ee8e0d45226b3236")

        val btnKakaoLogin = findViewById<Button>(R.id.btnKakaoLogin)

        btnKakaoLogin.setOnClickListener {
            loginWithKakao()
        }
    }

    private fun loginWithKakao() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (error != null) {
                Log.e("LoginActivity", "카카오 로그인 실패", error)
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                return@loginWithKakaoAccount
            }

            if (token != null) {
                Log.d("LoginActivity", "카카오 로그인 성공")
                handleKakaoLogin(token)
            }
        }
    }

    private fun handleKakaoLogin(token: OAuthToken) {

        val request = KakaoLoginRequest(access_token = token.accessToken)

        RetrofitClient.api.loginWithKakao(request)
            .enqueue(object : Callback<KakaoLoginResponse> {

                override fun onResponse(
                    call: Call<KakaoLoginResponse>,
                    response: Response<KakaoLoginResponse>
                ) {
                    if (!response.isSuccessful) {
                        Log.e("LoginActivity", "서버 오류 ${response.code()}")
                        return
                    }

                    val data = response.body()!!
                    Log.d("LoginActivity", "서버 로그인 성공 / profileComplete=${data.is_new_user}")

                    TokenManager.saveTokens(
                        this@LoginActivity,
                        data.access,
                        data.refresh ?: ""
                    )

                    // ⭐ 핵심 로직: 프로필 완료 여부에 따라 이동
                    if (data.is_new_user) {
                        // 신규유저 → 프로필 설정
                        startActivity(Intent(this@LoginActivity, ProfileSetupActivity::class.java))
                        Log.d("LoginActivity", "신규 유저 → ProfileSetupActivity 이동")
                    } else {
                        // 기존유저 → 메인
                        Log.d("LoginActivity", "기존 유저 → MainActivity 이동")
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    }
                    finish()
                }

                override fun onFailure(call: Call<KakaoLoginResponse>, t: Throwable) {
                    Log.e("LoginActivity", "Retrofit 실패", t)
                    Toast.makeText(this@LoginActivity, "서버 연결 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
