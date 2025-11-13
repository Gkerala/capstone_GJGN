package com.example.gjgn_02v

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KakaoSdk.init(this, "15717b2f42caeea1ee8e0d45226b3236")

        // ✅ JWT 존재 여부 확인 후 서버 검증
        val accessToken = TokenManager.getAccessToken(this)
        val refreshToken = TokenManager.getRefreshToken(this)

        if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
            verifyTokenWithServer(accessToken)
            return
        }

        // ✅ 새 로그인 시도
        loginWithKakao()
    }

    private fun verifyTokenWithServer(jwtToken: String) {
        RetrofitClient.api.getCurrentUser("Bearer $jwtToken")
            .enqueue(object : Callback<UserProfile> {
                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                    if (response.isSuccessful) {
                        val profile = response.body()
                        if (profile?.profile_completed == true) {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        } else {
                            val intent = Intent(this@LoginActivity, ProfileSetupActivity::class.java)
                            intent.putExtra("JWT_TOKEN", jwtToken)
                            intent.putExtra("USER_EMAIL", profile?.email ?: "")
                            startActivity(intent)
                        }
                        finish()
                    } else {
                        TokenManager.clearTokens(this@LoginActivity)
                        loginWithKakao()
                    }
                }

                override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                    TokenManager.clearTokens(this@LoginActivity)
                    loginWithKakao()
                }
            })
    }

    private fun loginWithKakao() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (token != null) handleKakaoLogin(token)
            else {
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                Log.e("LoginActivity", "카카오 로그인 실패: $error")
            }
        }
    }

    private fun handleKakaoLogin(token: OAuthToken) {
        val accessToken = token.accessToken
        val request = mapOf("access_token" to accessToken)

        RetrofitClient.api.loginWithKakao(request)
            .enqueue(object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        TokenManager.saveTokens(this@LoginActivity, body.access, body.refresh)

                        checkProfileAndProceed(body.access, body.user?.email ?: "unknown@kakao.com")
                    } else {
                        Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "네트워크 오류", Toast.LENGTH_LONG).show()
                    Log.e("LoginActivity", "카카오 로그인 실패", t)
                }
            })
    }

    private fun checkProfileAndProceed(jwtToken: String, email: String) {
        RetrofitClient.api.getCurrentUser("Bearer $jwtToken")
            .enqueue(object : Callback<UserProfile> {
                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                    if (response.isSuccessful) {
                        val profile = response.body()
                        if (profile?.profile_completed == true) {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        } else {
                            val intent = Intent(this@LoginActivity, ProfileSetupActivity::class.java)
                            intent.putExtra("JWT_TOKEN", jwtToken)
                            intent.putExtra("USER_EMAIL", email)
                            startActivity(intent)
                        }
                    } else {
                        goToProfileSetup(jwtToken, email)
                    }
                    finish()
                }

                override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                    goToProfileSetup(jwtToken, email)
                    finish()
                }
            })
    }

    private fun goToProfileSetup(jwtToken: String, email: String) {
        val intent = Intent(this@LoginActivity, ProfileSetupActivity::class.java)
        intent.putExtra("JWT_TOKEN", jwtToken)
        intent.putExtra("USER_EMAIL", email)
        startActivity(intent)
    }
}
