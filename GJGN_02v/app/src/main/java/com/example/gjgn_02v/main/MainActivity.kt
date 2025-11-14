package com.example.gjgn_02v.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.GoalStatResponse
import com.example.gjgn_02v.data.model.records.MealRecordResponse
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
        loadWeeklyStat()
        loadMonthlyStat()
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

    // 오늘 요약
    private fun loadTodayRecords() {
        RetrofitClient.api.getTodayRecords()
            .enqueue(object : Callback<List<MealRecordResponse>> {
                override fun onResponse(
                    call: Call<List<MealRecordResponse>>,
                    response: Response<List<MealRecordResponse>>
                ) {
                    if (!response.isSuccessful || response.body() == null) return

                    val list = response.body()!!

                    val totalKcal = list.sumOf { it.calories }
                    val count = list.size

                    tvTodayKcal.text = "$totalKcal kcal"
                    tvTodayCount.text = "$count 회"

                    // 최신 2개만 ID 기반으로 표시
                    if (list.isNotEmpty()) tvRecent1.text = "음식 #${list[0].food_id}"
                    if (list.size >= 2) tvRecent2.text = "음식 #${list[1].food_id}"
                }

                override fun onFailure(call: Call<List<MealRecordResponse>>, t: Throwable) {}
            })
    }

    // 주간 달성률
    private fun loadWeeklyStat() {
        RetrofitClient.api.getWeeklyAchievement()
            .enqueue(object : Callback<GoalStatResponse> {
                override fun onResponse(
                    call: Call<GoalStatResponse>,
                    res: Response<GoalStatResponse>
                ) {
                    if (res.isSuccessful) {
                        tvWeekly.text = "${res.body()!!.achievement}%"
                    }
                }
                override fun onFailure(call: Call<GoalStatResponse>, t: Throwable) {}
            })
    }

    // 월간 달성률
    private fun loadMonthlyStat() {
        RetrofitClient.api.getMonthlyAchievement()
            .enqueue(object : Callback<GoalStatResponse> {
                override fun onResponse(
                    call: Call<GoalStatResponse>,
                    res: Response<GoalStatResponse>
                ) {
                    if (res.isSuccessful) {
                        tvMonthly.text = "${res.body()!!.achievement}%"
                    }
                }
                override fun onFailure(call: Call<GoalStatResponse>, t: Throwable) {}
            })
    }

    // 네비게이션
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        bottomNav.selectedItemId = R.id.menu_main

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> return@setOnItemSelectedListener true
                R.id.menu_record -> startActivity(Intent(this, RecordActivity::class.java))
                R.id.menu_analysis -> startActivity(Intent(this, AnalysisActivity::class.java))
                R.id.menu_mypage -> startActivity(Intent(this, MyPageActivity::class.java))
            }
            true
        }
    }
}
