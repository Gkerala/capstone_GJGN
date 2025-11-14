package com.example.gjgn_02v.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.gjgn_02v.main.MainActivity
import com.example.gjgn_02v.profile.ProfileSetupActivity
import com.example.gjgn_02v.utils.TokenManager
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.KakaoLoginRequest
import com.example.gjgn_02v.data.model.auth.KakaoLoginResponse
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 카카오 SDK 초기화
        KakaoSdk.init(this, "15717b2f42caeea1ee8e0d45226b3236")

        val access = TokenManager.getAccessToken(this)
        val refresh = TokenManager.getRefreshToken(this)

        // 기존 JWT가 있으면 자동 로그인 시도
        if (!access.isNullOrEmpty() && !refresh.isNullOrEmpty()) {
            verifyTokenWithServer(access)
            return
        }

        // 없으면 카카오 로그인
        loginWithKakao()
    }

    /**
     * 서버에 JWT 유효성 검증 요청
     */
    private fun verifyTokenWithServer(jwt: String) {
        RetrofitClient.api.getCurrentUser("Bearer $jwt")
            .enqueue(object : Callback<UserProfileResponse> {

                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        // 토큰 유효함 → 바로 메인 페이지로 이동
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        // 토큰 만료 → 다시 로그인
                        TokenManager.clearTokens(this@LoginActivity)
                        loginWithKakao()
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    TokenManager.clearTokens(this@LoginActivity)
                    loginWithKakao()
                }
            })
    }

    /**
     * 카카오 로그인 → 액세스 토큰 받아오는 단계
     */
    private fun loginWithKakao() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (token != null) {
                handleKakaoLogin(token)
            } else {
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                Log.e("LoginActivity", "카카오 로그인 실패: $error")
            }
        }
    }

    /**
     * 카카오 Access Token → 서버로 전달하여 JWT 발급받는 단계
     */
    private fun handleKakaoLogin(token: OAuthToken) {

        val request = KakaoLoginRequest(
            kakao_token = token.accessToken     // 서버 요구 파라미터명 기준
        )

        RetrofitClient.api.loginWithKakao(request)
            .enqueue(object : Callback<KakaoLoginResponse> {

                override fun onResponse(
                    call: Call<KakaoLoginResponse>,
                    response: Response<KakaoLoginResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {

                        val data = response.body()!!

                        // JWT 저장
                        TokenManager.saveTokens(
                            this@LoginActivity,
                            data.access,
                            data.refresh ?: ""
                        )

                        // 신규 유저인지 여부에 따라 분기
                        if (data.is_new_user) {
                            // 프로필 설정 페이지로 이동
                            startActivity(Intent(this@LoginActivity, ProfileSetupActivity::class.java))

                        } else {
                            // 기존 유저 → 바로 메인 화면
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        }

                        finish()

                    } else {
                        Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<KakaoLoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "서버 연결 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
