package com.example.gjgn_02v.main

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.GoalStatResponse
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnalysisActivity : AppCompatActivity() {

    private lateinit var tvWeekly: TextView
    private lateinit var tvMonthly: TextView
    private lateinit var barChartCalories: BarChart
    private lateinit var lineChartWeight: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        initViews()
        loadWeeklyStat()
        loadMonthlyStat()
        loadWeeklyCharts()
    }

    private fun initViews() {
        tvWeekly = findViewById(R.id.tvWeeklyAchieve)
        tvMonthly = findViewById(R.id.tvMonthlyAchieve)

        barChartCalories = findViewById(R.id.barChartCalories)
        lineChartWeight = findViewById(R.id.lineChartWeight)
    }

    // ───────────────────────────────────────────────
    // 📌 주간 달성률
    // ───────────────────────────────────────────────
    private fun loadWeeklyStat() {
        RetrofitClient.api.getWeeklyAchievement()
            .enqueue(object : Callback<GoalStatResponse> {
                override fun onResponse(
                    call: Call<GoalStatResponse>,
                    res: Response<GoalStatResponse>
                ) {
                    if (res.isSuccessful && res.body() != null) {
                        tvWeekly.text = "주간 달성률: ${res.body()!!.achievement}%"
                    }
                }

                override fun onFailure(call: Call<GoalStatResponse>, t: Throwable) {}
            })
    }

    // ───────────────────────────────────────────────
    // 📌 월간 달성률
    // ───────────────────────────────────────────────
    private fun loadMonthlyStat() {
        RetrofitClient.api.getMonthlyAchievement()
            .enqueue(object : Callback<GoalStatResponse> {
                override fun onResponse(
                    call: Call<GoalStatResponse>,
                    res: Response<GoalStatResponse>
                ) {
                    if (res.isSuccessful && res.body() != null) {
                        tvMonthly.text = "월간 달성률: ${res.body()!!.achievement}%"
                    }
                }

                override fun onFailure(call: Call<GoalStatResponse>, t: Throwable) {}
            })
    }

    // ───────────────────────────────────────────────
    // 📌 차트 데이터 불러오기
    // ───────────────────────────────────────────────
    private fun loadWeeklyCharts() {

        RetrofitClient.api.getWeeklyAchievement()   // 같은 API에서 calories/weights도 가져온다고 가정함
            .enqueue(object : Callback<GoalStatResponse> {
                override fun onResponse(
                    call: Call<GoalStatResponse>,
                    res: Response<GoalStatResponse>
                ) {
                    if (!res.isSuccessful || res.body() == null) {
                        Toast.makeText(this@AnalysisActivity, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val data = res.body()!!

                    setupCalorieChart(data.calories)
                    setupWeightChart(data.weights)
                }

                override fun onFailure(call: Call<GoalStatResponse>, t: Throwable) {}
            })
    }

    // ───────────────────────────────────────────────
    // 📊 칼로리 바차트
    // ───────────────────────────────────────────────
    private fun setupCalorieChart(calories: List<Int>) {

        val entries = calories.mapIndexed { index, value ->
            BarEntry(index.toFloat(), value.toFloat())
        }

        val barDataSet = BarDataSet(entries, "일일 섭취 칼로리")
        barDataSet.color = resources.getColor(R.color.teal_700, null)

        val barData = BarData(barDataSet)
        barData.barWidth = 0.4f

        barChartCalories.data = barData
        barChartCalories.setFitBars(true)

        val desc = Description()
        desc.text = "최근 7일 칼로리"
        barChartCalories.description = desc

        barChartCalories.invalidate()
    }

    // ───────────────────────────────────────────────
    // 📈 체중 라인차트
    // ───────────────────────────────────────────────
    private fun setupWeightChart(weights: List<Float>) {

        val entries = weights.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val lineDataSet = LineDataSet(entries, "체중 변화 (kg)")
        lineDataSet.color = resources.getColor(R.color.purple_500, null)
        lineDataSet.circleColor = resources.getColor(R.color.purple_500, null)
        lineDataSet.lineWidth = 2f
        lineDataSet.circleRadius = 4f

        val lineData = LineData(lineDataSet)

        val desc = Description()
        desc.text = "최근 7일 체중 변화"
        lineChartWeight.description = desc

        lineChartWeight.data = lineData
        lineChartWeight.invalidate()
    }
}
