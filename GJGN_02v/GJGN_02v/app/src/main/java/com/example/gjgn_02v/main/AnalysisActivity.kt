package com.example.gjgn_02v.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.analysis.WeeklyAnalysisResponse
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

        barChartWeekly = findViewById(R.id.barChartWeekly)
        lineChartWeight = findViewById(R.id.lineChartWeight)
        tvWeeklyPercent = findViewById(R.id.tvWeeklyPercent)
        tvMonthlyPercent = findViewById(R.id.tvMonthlyPercent)

        loadWeeklyCalories()
        loadWeeklyWeight()
        loadWeeklyStat()
        loadMonthlyStat()

        setupBottomNav()
    }

    // ----------------------------------------------------------------
    // 📊 주간 칼로리 그래프
    // ----------------------------------------------------------------
    private fun loadWeeklyCalories() {
        RetrofitClient.api.getWeeklyAnalysis()
            .enqueue(object : Callback<WeeklyAnalysisResponse> {
                override fun onResponse(
                    call: Call<WeeklyAnalysisResponse>,
                    res: Response<WeeklyAnalysisResponse>
                ) {
                    if (!res.isSuccessful || res.body() == null) return

                    val list = res.body()?.weekly_records ?: emptyList()

                    val entries = list.mapIndexed { i, day ->
                        BarEntry(i.toFloat(), day.calories.toFloat())
                    }

                    val dataSet = BarDataSet(entries, "주간 칼로리")
                    val barData = BarData(dataSet)

                    barChartWeekly.data = barData
                    barChartWeekly.description.isEnabled = false
                    barChartWeekly.invalidate()
                }

                override fun onFailure(call: Call<WeeklyAnalysisResponse>, t: Throwable) {
                    Log.e("WEEKLY_CAL", "fail", t)
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
                    Log.d("WEEKLY_WEIGHT", "========== API RESPONSE ==========")
                    Log.d("WEEKLY_WEIGHT", "HTTP CODE = ${res.code()}")
                    Log.d("WEEKLY_WEIGHT", "RAW BODY = ${res.raw()}")

                    if (!res.isSuccessful) {
                        Log.e("WEEKLY_WEIGHT", "Response not successful")
                        lineChartWeight.clear()
                        lineChartWeight.invalidate()
                        return
                    }

                    val body = res.body()
                    Log.d("WEEKLY_WEIGHT", "Parsed Body = $body")

                    if (body == null) {
                        Log.e("WEEKLY_WEIGHT", "Body is null")
                        lineChartWeight.clear()
                        lineChartWeight.invalidate()
                        return
                    }

                    val weightList = body.records ?: emptyList()
                    Log.d("WEEKLY_WEIGHT", "Weight List = $weightList")
                    Log.d("WEEKLY_WEIGHT", "List Size = ${weightList.size}")

                    if (weightList.isEmpty()) {
                        Log.w("WEEKLY_WEIGHT", "Weight list is EMPTY")
                        lineChartWeight.clear()
                        lineChartWeight.invalidate()
                        return
                    }

                    val entries = weightList.mapIndexedNotNull { index, item ->
                        item.weight?.let { weightValue ->
                            Entry(index.toFloat(), weightValue)
                        }
                    }

                    Log.d("WEEKLY_WEIGHT", "Entries = $entries")

                    if (entries.isEmpty()) {
                        Log.w("WEEKLY_WEIGHT", "Entries list is EMPTY")
                        lineChartWeight.clear()
                        lineChartWeight.invalidate()
                        return
                    }

                    val dataSet = LineDataSet(entries, "체중 변화 (kg)").apply {
                        circleRadius = 4f
                        lineWidth = 3f
                    }

                    lineChartWeight.data = LineData(dataSet)
                    lineChartWeight.description.isEnabled = false
                    lineChartWeight.invalidate()

                    Log.d("WEEKLY_WEIGHT", "Chart updated successfully!")
                }

                override fun onFailure(call: Call<WeeklyWeightResponse>, t: Throwable) {
                    Log.e("WEEKLY_WEIGHT", "API FAILED: ${t.message}")
                    lineChartWeight.clear()
                    lineChartWeight.invalidate()
                }
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
                    tvWeeklyPercent.text =
                        if (res.isSuccessful && res.body() != null)
                            "${res.body()!!.achievement}%"
                        else "0%"
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
                    tvMonthlyPercent.text =
                        if (res.isSuccessful && res.body() != null)
                            "${res.body()!!.achievement}%"
                        else "0%"
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
                R.id.menu_record -> startActivity(Intent(this, RecordSelectActivity::class.java))
                R.id.menu_analysis -> return@setOnItemSelectedListener true
                R.id.menu_mypage -> startActivity(Intent(this, MyPageActivity::class.java))
            }
            true
        }
    }
}
