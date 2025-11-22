package com.example.gjgn_02v.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.utils.TokenManager
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.UserGoalResponse
import com.example.gjgn_02v.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageActivity : AppCompatActivity() {

    private lateinit var btnLogout: Button
    private lateinit var btnDelete: Button
    private lateinit var btnEditProfile: Button

    private lateinit var tvKcal: TextView
    private lateinit var tvCarb: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvFat: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        // 버튼
        btnLogout = findViewById(R.id.btnLogout)
        btnDelete = findViewById(R.id.btnDeleteAccount)
        btnEditProfile = findViewById(R.id.btnEditProfile)

        // 영양정보 TextView
        tvKcal = findViewById(R.id.tvKcal)
        tvCarb = findViewById(R.id.tvCarb)
        tvProtein = findViewById(R.id.tvProtein)
        tvFat = findViewById(R.id.tvFat)

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }

        btnLogout.setOnClickListener { logoutUser() }
        btnDelete.setOnClickListener { deleteUser() }

        // 🔥 여기서 목표(영양) 정보 가져오기
        loadUserGoal()

        setBottomNav()
    }

    private fun loadUserGoal() {
        RetrofitClient.api.getGoal().enqueue(object : Callback<UserGoalResponse> {
            override fun onResponse(
                call: Call<UserGoalResponse>,
                response: Response<UserGoalResponse>
            ) {
                if (response.isSuccessful) {
                    val goal = response.body()!!

                    tvKcal.text = "${goal.target_kcal} kcal"
                    tvCarb.text = "${goal.target_carb} g"
                    tvProtein.text = "${goal.target_protein} g"
                    tvFat.text = "${goal.target_fat} g"
                } else {
                    Toast.makeText(this@MyPageActivity, "영양 정보 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserGoalResponse>, t: Throwable) {
                Toast.makeText(this@MyPageActivity, "서버 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logoutUser() {
        TokenManager.clearTokens(this)
        startActivity(Intent(this, LoginActivity::class.java)
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
        finish()
        Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun deleteUser() {
        RetrofitClient.api.deleteUser()
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    handleLogoutSuccess("회원탈퇴 완료")
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    handleLogoutSuccess("회원탈퇴 실패 — 강제 로그아웃")
                }
            })
    }

    private fun handleLogoutSuccess(msg: String) {
        TokenManager.clearTokens(this)
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, LoginActivity::class.java)
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
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
