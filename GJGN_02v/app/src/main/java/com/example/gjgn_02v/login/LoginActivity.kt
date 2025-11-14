package com.example.gjgn_02v.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.gjgn_02v.main.MainActivity
import com.example.gjgn_02v.profile.ProfileSetupActivity
import com.example.gjgn_02v.utils.TokenManager
import com.example.gjgn_02v.utils.TokenResponse
import com.example.gjgn_02v.utils.UserProfile
import com.example.gjgn_02v.data.api.RetrofitClient
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

        val access = TokenManager.getAccessToken(this)
        val refresh = TokenManager.getRefreshToken(this)

        if (!access.isNullOrEmpty() && !refresh.isNullOrEmpty()) {
            verifyTokenWithServer(access)
            return
        }

        loginWithKakao()
    }

    private fun verifyTokenWithServer(jwt: String) {
        RetrofitClient.api.getCurrentUser("Bearer $jwt")
            .enqueue(object : Callback<UserProfile> {
                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                    if (response.isSuccessful) {
                        moveAfterLogin(jwt, response.body())
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
        val req = mapOf("access_token" to token.accessToken)

        RetrofitClient.api.loginWithKakao(req)
            .enqueue(object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, res: Response<TokenResponse>) {
                    if (res.isSuccessful && res.body() != null) {
                        val data = res.body()!!
                        TokenManager.saveTokens(this@LoginActivity, data.access, data.refresh)
                        moveAfterLogin(data.access, data.user)
                    } else {
                        Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "네트워크 오류", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun moveAfterLogin(jwt: String, profile: UserProfile?) {
        if (profile?.profile_completed == true) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            val intent = Intent(this, ProfileSetupActivity::class.java)
            intent.putExtra("JWT_TOKEN", jwt)
            intent.putExtra("USER_EMAIL", profile?.email ?: "")
            startActivity(intent)
        }
        finish()
    }
}
