package com.example.gjgn_02v.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.GoalStatResponse
import com.example.gjgn_02v.data.model.goals.WeeklyWeightResponse
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnalysisActivity : AppCompatActivity() {

    private lateinit var barChartWeekly: BarChart
    private lateinit var lineChartWeight: LineChart
    private lateinit var tvWeeklyPercent: TextView
    private lateinit var tvMonthlyPercent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        // UI 연결
        barChartWeekly = findViewById(R.id.barChartWeekly)
        lineChartWeight = findViewById(R.id.lineChartWeight)

        tvWeeklyPercent = findViewById(R.id.tvWeeklyPercent)
        tvMonthlyPercent = findViewById(R.id.tvMonthlyPercent)

        // 데이터 로드
        loadWeeklyCalories()
        loadWeeklyWeight()
        loadWeeklyStat()
        loadMonthlyStat()

        // 네비게이션 적용
        setupBottomNav()
    }

    // ----------------------------------------------------------------
    // 📊 주간 섭취 칼로리 그래프
    // ----------------------------------------------------------------
    private fun loadWeeklyCalories() {
        RetrofitClient.api.getRecordsByDate("weekly")
            .enqueue(object : Callback<List<com.example.gjgn_02v.data.model.records.MealRecordResponse>> {
                override fun onResponse(
                    call: Call<List<com.example.gjgn_02v.data.model.records.MealRecordResponse>>,
                    res: Response<List<com.example.gjgn_02v.data.model.records.MealRecordResponse>>
                ) {
                    if (!res.isSuccessful || res.body().isNullOrEmpty()) return
                    val list = res.body()!!

                    val entries = list.mapIndexed { i, rec ->
                        BarEntry(i.toFloat(), rec.calories.toFloat())
                    }

                    val dataSet = BarDataSet(entries, "일일 섭취 칼로리")
                    dataSet.color = resources.getColor(R.color.teal_700)

                    barChartWeekly.data = BarData(dataSet)
                    barChartWeekly.description.isEnabled = false
                    barChartWeekly.invalidate()
                }

                override fun onFailure(
                    call: Call<List<com.example.gjgn_02v.data.model.records.MealRecordResponse>>,
                    t: Throwable
                ) {
                }
            })
    }

    // ----------------------------------------------------------------
    // ⚖️ 주간 체중 변화 그래프
    // ----------------------------------------------------------------
    private fun loadWeeklyWeight() {
        RetrofitClient.api.getWeeklyWeight()
            .enqueue(object : Callback<WeeklyWeightResponse> {
                override fun onResponse(
                    call: Call<WeeklyWeightResponse>,
                    res: Response<WeeklyWeightResponse>
                ) {
                    if (!res.isSuccessful || res.body() == null) return

                    val weightList = res.body()!!.weekly_weight

                    val entries = weightList.mapIndexedNotNull { index, item ->
                        item.weight?.let { Entry(index.toFloat(), it) }
                    }

                    if (entries.isEmpty()) return

                    val set = LineDataSet(entries, "체중 변화 (kg)")
                    set.color = resources.getColor(R.color.purple_700)
                    set.setCircleColor(resources.getColor(R.color.purple_700))
                    set.circleRadius = 4f
                    set.lineWidth = 3f

                    lineChartWeight.data = LineData(set)
                    lineChartWeight.description.isEnabled = false
                    lineChartWeight.invalidate()
                }

                override fun onFailure(call: Call<WeeklyWeightResponse>, t: Throwable) {}
            })
    }

    // ----------------------------------------------------------------
    // 🎯 주간 달성률
    // ----------------------------------------------------------------
    private fun loadWeeklyStat() {
        RetrofitClient.api.getWeeklyAchievement()
            .enqueue(object : Callback<GoalStatResponse> {
                override fun onResponse(
                    call: Call<GoalStatResponse>,
                    res: Response<GoalStatResponse>
                ) {
                    if (res.isSuccessful && res.body() != null) {
                        tvWeeklyPercent.text = "${res.body()!!.achievement}%"
                    } else tvWeeklyPercent.text = "0%"
                }

                override fun onFailure(call: Call<GoalStatResponse>, t: Throwable) {
                    tvWeeklyPercent.text = "0%"
                }
            })
    }

    // ----------------------------------------------------------------
    // 🎯 월간 달성률
    // ----------------------------------------------------------------
    private fun loadMonthlyStat() {
        RetrofitClient.api.getMonthlyAchievement()
            .enqueue(object : Callback<GoalStatResponse> {
                override fun onResponse(
                    call: Call<GoalStatResponse>,
                    res: Response<GoalStatResponse>
                ) {
                    if (res.isSuccessful && res.body() != null) {
                        tvMonthlyPercent.text = "${res.body()!!.achievement}%"
                    } else tvMonthlyPercent.text = "0%"
                }

                override fun onFailure(call: Call<GoalStatResponse>, t: Throwable) {
                    tvMonthlyPercent.text = "0%"
                }
            })
    }

    // ----------------------------------------------------------------
    // ⬇️ 하단 네비게이션
    // ----------------------------------------------------------------
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        bottomNav.selectedItemId = R.id.menu_analysis

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> startActivity(Intent(this, MainActivity::class.java))
                R.id.menu_record -> startActivity(Intent(this, RecordActivity::class.java))
                R.id.menu_analysis -> return@setOnItemSelectedListener true
                R.id.menu_mypage -> startActivity(Intent(this, MyPageActivity::class.java))
            }
            true
        }
    }
}
