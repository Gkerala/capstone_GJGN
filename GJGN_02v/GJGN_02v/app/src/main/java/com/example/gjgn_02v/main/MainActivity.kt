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
            .enqueue(object : Callback<List<MealRecordResponse>> {
                override fun onResponse(
                    call: Call<List<MealRecordResponse>>,
                    response: Response<List<MealRecordResponse>>
                ) {
                    if (!response.isSuccessful || response.body() == null) {
                        tvTodayKcal.text = "0 kcal"
                        tvTodayCount.text = "0 회"
                        return
                    }

                    val list = response.body()!!
                    val totalKcal = list.sumOf { it.calories }
                    val count = list.size

                    tvTodayKcal.text = "$totalKcal kcal"
                    tvTodayCount.text = "$count 회"

                }

                override fun onFailure(call: Call<List<MealRecordResponse>>, t: Throwable) {
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
