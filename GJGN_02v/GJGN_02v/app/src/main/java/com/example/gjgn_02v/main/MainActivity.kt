package com.example.gjgn_02v.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.GoalStatResponse
import com.example.gjgn_02v.data.model.records.MealRecordResponse
import com.example.gjgn_02v.data.model.records.TodayStatResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var tvTodayKcal: TextView
    private lateinit var tvTodayCount: TextView
    private lateinit var tvWeekly: TextView
    private lateinit var tvMonthly: TextView
    private lateinit var tvRecent1: TextView
    private lateinit var tvRecent2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadTodayRecords()
        setupBottomNav()
    }

    private fun initViews() {
        tvTodayKcal = findViewById(R.id.tvTodayKcal)
        tvTodayCount = findViewById(R.id.tvTodayCount)
        tvWeekly = findViewById(R.id.tvWeekly)
        tvMonthly = findViewById(R.id.tvMonthly)
        tvRecent1 = findViewById(R.id.tvRecentFood1)
        tvRecent2 = findViewById(R.id.tvRecentFood2)
    }


    // 🔥 오늘 기록 불러오기
    private fun loadTodayRecords() {
        RetrofitClient.api.getTodayRecords()
            .enqueue(object : Callback<TodayStatResponse> {
                override fun onResponse(
                    call: Call<TodayStatResponse>,
                    response: Response<TodayStatResponse>
                ) {
                    if (!response.isSuccessful || response.body() == null) {
                        tvTodayKcal.text = "0 kcal"
                        tvTodayCount.text = "0 회"
                        return
                    }

                    val data = response.body()!!
                    tvTodayKcal.text = "${data.total_kcal} kcal"
                    tvTodayCount.text = "${data.count} 회"

                    if (data.recent.isNotEmpty()) {
                        tvRecent1.text = data.recent.getOrNull(0) ?: "-"
                        tvRecent2.text = data.recent.getOrNull(1) ?: "-"
                    }
                }

                override fun onFailure(call: Call<TodayStatResponse>, t: Throwable) {
                    tvTodayKcal.text = "0 kcal"
                    tvTodayCount.text = "0 회"
                }
            })
    }



    // 하단 네비게이션
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        bottomNav.selectedItemId = R.id.menu_main

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> return@setOnItemSelectedListener true
                R.id.menu_record -> startActivity(Intent(this, RecordSelectActivity::class.java))
                R.id.menu_analysis -> startActivity(Intent(this, AnalysisActivity::class.java))
                R.id.menu_mypage -> startActivity(Intent(this, MyPageActivity::class.java))
            }
            true
        }
    }
}
