package com.example.gjgn_02v.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.utils.TokenManager
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageActivity : AppCompatActivity() {

    private lateinit var btnLogout: Button
    private lateinit var btnDeleteAccount: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        btnLogout = findViewById(R.id.btnLogout)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)

        // ✅ 로그아웃 버튼
        btnLogout.setOnClickListener {
            val access = TokenManager.getAccessToken(this)
            val refresh = TokenManager.getRefreshToken(this)

            if (access != null && refresh != null) {
                RetrofitClient.api.logoutUser(
                    "Bearer $access",
                    mapOf("refresh_token" to refresh)
                ).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        handleLogoutOrDeleteSuccess("로그아웃 완료")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        handleLogoutOrDeleteSuccess("네트워크 오류로 로그아웃 처리됨")
                    }
                })
            } else {
                handleLogoutOrDeleteSuccess("토큰 없음 — 강제 로그아웃")
            }
        }

        // ✅ 회원 탈퇴 버튼
        btnDeleteAccount.setOnClickListener {
            val access = TokenManager.getAccessToken(this)
            val refresh = TokenManager.getRefreshToken(this)

            if (access != null && refresh != null) {
                RetrofitClient.api.deleteUser(
                    "Bearer $access",
                    mapOf("refresh_token" to refresh)
                ).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        performLogoutAfterDelete(refresh)
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        handleLogoutOrDeleteSuccess("회원탈퇴 요청 실패 — 토큰 초기화 후 복귀")
                    }
                })
            } else {
                handleLogoutOrDeleteSuccess("토큰 없음 — 강제 로그아웃")
            }
        }

        // ✅ 하단 네비게이션
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        bottomNav.selectedItemId = R.id.menu_mypage

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> startActivity(Intent(this, MainActivity::class.java))
                R.id.menu_record -> startActivity(Intent(this, RecordActivity::class.java))
                R.id.menu_analysis -> startActivity(Intent(this, AnalysisActivity::class.java))
                R.id.menu_mypage -> true
                else -> false
            }
            true
        }
    }

    private fun performLogoutAfterDelete(refresh: String) {
        val access = TokenManager.getAccessToken(this)
        if (access != null) {
            RetrofitClient.api.logoutUser("Bearer $access", mapOf("refresh_token" to refresh))
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        handleLogoutOrDeleteSuccess("회원탈퇴 및 로그아웃 완료")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        handleLogoutOrDeleteSuccess("회원탈퇴 완료 (로그아웃 실패 무시)")
                    }
                })
        } else {
            handleLogoutOrDeleteSuccess("회원탈퇴 완료 (토큰 없음)")
        }
    }

    private fun handleLogoutOrDeleteSuccess(message: String) {
        TokenManager.clearTokens(this)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}