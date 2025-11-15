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

        // Kakao SDK Init
        KakaoSdk.init(this, "15717b2f42caeea1ee8e0d45226b3236")
        Log.d("LoginActivity", "Kakao SDK Initialized")

        val btnKakaoLogin = findViewById<Button>(R.id.btnKakaoLogin)

        // 카카오 로그인 버튼 클릭
        btnKakaoLogin.setOnClickListener {
            Log.d("LoginActivity", "카카오 로그인 버튼 클릭됨")
            loginWithKakao()
        }
    }

    /**
     * 카카오 로그인 → 액세스 토큰 받아오는 단계
     */
    private fun loginWithKakao() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->

            if (error != null) {
                Log.e("LoginActivity", "카카오 로그인 실패: $error")
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                return@loginWithKakaoAccount
            }

            if (token != null) {
                Log.d("LoginActivity", "카카오 로그인 성공: ${token.accessToken}")
                handleKakaoLogin(token)
            }
        }
    }

    /**
     * 카카오 Access Token → 서버로 전달하여 JWT 발급받는 단계
     */
    private fun handleKakaoLogin(token: OAuthToken) {

        val request = KakaoLoginRequest(
            access_token = token.accessToken
        )

        Log.d("LoginActivity", "서버로 로그인 요청 전송")

        RetrofitClient.api.loginWithKakao(request)
            .enqueue(object : Callback<KakaoLoginResponse> {

                override fun onResponse(
                    call: Call<KakaoLoginResponse>,
                    response: Response<KakaoLoginResponse>
                ) {

                    if (!response.isSuccessful) {
                        Log.e(
                            "LoginActivity",
                            "서버 로그인 실패 code=${response.code()} body=${response.errorBody()?.string()}"
                        )
                        Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val data = response.body()
                    if (data == null) {
                        Toast.makeText(this@LoginActivity, "서버 오류 발생", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", "response.body() is NULL")
                        return
                    }

                    // ⭐ 토큰 저장
                    TokenManager.saveTokens(
                        this@LoginActivity,
                        data.access,
                        data.refresh ?: ""
                    )

                    Log.d(
                        "LoginActivity",
                        "JWT 저장됨 Access=${data.access.substring(0, 10)}..."
                    )

                    // ⭐ 신규 유저 / 기존 유저 분기
                    if (data.is_new_user) {
                        Log.d("LoginActivity", "신규 유저 → ProfileSetupActivity 이동")
                        startActivity(Intent(this@LoginActivity, ProfileSetupActivity::class.java))
                    } else {
                        Log.d("LoginActivity", "기존 유저 → MainActivity 이동")
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    }

                    finish()   // 현재 로그인 페이지 종료
                }

                override fun onFailure(call: Call<KakaoLoginResponse>, t: Throwable) {
                    Log.e("LoginActivity", "Retrofit 실패: ${t.message}")
                    Toast.makeText(this@LoginActivity, "서버 연결 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
