package com.example.gjgn_02v.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.analysis.WeeklyAnalysisResponse
import com.example.gjgn_02v.data.model.goals.WeeklyWeightResponse
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AnalysisActivity : AppCompatActivity() {

    private lateinit var barChartWeekly: BarChart
    private lateinit var lineChartWeight: LineChart
    private lateinit var tvWeekRange: TextView

    private lateinit var btnPrevWeek: ImageView
    private lateinit var btnNextWeek: ImageView

    // API 24 호환 Calendar 기반 날짜
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        barChartWeekly = findViewById(R.id.barChartWeekly)
        lineChartWeight = findViewById(R.id.lineChartWeight)
        tvWeekRange = findViewById(R.id.tvWeekRange)

        btnPrevWeek = findViewById(R.id.btnPrevWeek)
        btnNextWeek = findViewById(R.id.btnNextWeek)

        setupWeekSelector()
        loadWeeklyData()

        setupBottomNav()
    }

    // ----------------------------------------------------------------
    // 🔁 날짜 계산 (API 24 호환)
    // ----------------------------------------------------------------

    private fun getCurrentMonday(): Calendar {
        val cal = calendar.clone() as Calendar
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

        // Calendar: 일=1, 월=2...
        val diff = if (dayOfWeek == Calendar.SUNDAY) -6 else (Calendar.MONDAY - dayOfWeek)

        cal.add(Calendar.DAY_OF_MONTH, diff)
        return cal
    }

    private fun updateWeekRangeText() {
        val monday = getCurrentMonday()
        val sunday = (monday.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 6)
        }

        val sdf = SimpleDateFormat("MM.dd", Locale.KOREA)
        tvWeekRange.text = "${sdf.format(monday.time)} ~ ${sdf.format(sunday.time)}"
    }

    private fun setupWeekSelector() {
        updateWeekRangeText()

        btnPrevWeek.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeekRangeText()
            loadWeeklyData()
        }

        btnNextWeek.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeekRangeText()
            loadWeeklyData()
        }
    }

    private fun loadWeeklyData() {
        loadWeeklyCalories()
        loadWeeklyWeight()
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
                    if (!res.isSuccessful || res.body() == null) {
                        lineChartWeight.clear()
                        lineChartWeight.invalidate()
                        return
                    }

                    val weightList = res.body()?.records ?: emptyList()

                    val entries = weightList.mapIndexedNotNull { idx, item ->
                        item.weight?.let { weightValue ->
                            Entry(idx.toFloat(), weightValue)
                        }
                    }

                    if (entries.isEmpty()) {
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
                }

                override fun onFailure(call: Call<WeeklyWeightResponse>, t: Throwable) {
                    lineChartWeight.clear()
                    lineChartWeight.invalidate()
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
                R.id.menu_main ->
                    startActivity(Intent(this, MainActivity::class.java))

                R.id.menu_record ->
                    startActivity(Intent(this, RecordSelectActivity::class.java))

                R.id.menu_analysis ->
                    return@setOnItemSelectedListener true

                R.id.menu_mypage ->
                    startActivity(Intent(this, MyPageActivity::class.java))
            }
            true
        }
    }
}
