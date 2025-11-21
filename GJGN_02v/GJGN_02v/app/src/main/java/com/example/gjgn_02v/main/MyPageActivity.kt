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
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        btnLogout = findViewById(R.id.btnLogout)
        btnDelete = findViewById(R.id.btnDeleteAccount)

        btnLogout.setOnClickListener { logoutUser() }
        btnDelete.setOnClickListener { deleteUser() }

        setBottomNav()
    }

    private fun logoutUser() {
        // 1) 토큰 삭제
        TokenManager.clearTokens(this)

        // 2) 로그인 화면으로 이동
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        // 3) 안내 메시지
        Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
    }


    private fun deleteUser() {
        val access = TokenManager.getAccessToken(this)
        val refresh = TokenManager.getRefreshToken(this)

        if (access == null || refresh == null) {
            handleLogoutSuccess("회원탈퇴 완료(토큰 없음)")
            return
        }

        RetrofitClient.api.deleteUser("Bearer $access", mapOf("refresh_token" to refresh))
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, res: Response<Void>) {
                    logoutUser()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    handleLogoutSuccess("회원탈퇴 실패 — 강제 로그아웃")
                }
            })
    }

    private fun handleLogoutSuccess(msg: String) {
        TokenManager.clearTokens(this)
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        bottomNav.selectedItemId = R.id.menu_mypage

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> startActivity(Intent(this, MainActivity::class.java))
                R.id.menu_record -> startActivity(Intent(this, RecordSelectActivity::class.java))
                R.id.menu_analysis -> startActivity(Intent(this, AnalysisActivity::class.java))
                R.id.menu_mypage -> return@setOnItemSelectedListener true
            }
            true
        }
    }
}
