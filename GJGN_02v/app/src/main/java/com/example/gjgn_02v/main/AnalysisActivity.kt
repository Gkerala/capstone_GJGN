package com.example.gjgn_02v.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.WeeklyWeightResponse
import com.example.gjgn_02v.data.model.records.MealRecordResponse
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnalysisActivity : AppCompatActivity() {

    private lateinit var barChartWeekly: BarChart
    private lateinit var lineChartWeight: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        barChartWeekly = findViewById(R.id.barChartWeekly)
        lineChartWeight = findViewById(R.id.lineChartWeight)

        loadWeeklyCalories()
        loadWeeklyWeight()
    }

    // ───────────────────────────────────────────────
    // 📊 1) 주간 칼로리 막대 그래프
    // ───────────────────────────────────────────────
    private fun loadWeeklyCalories() {
        RetrofitClient.api.getRecordsByDate("weekly")
            .enqueue(object : Callback<List<MealRecordResponse>> {
                override fun onResponse(
                    call: Call<List<MealRecordResponse>>,
                    res: Response<List<MealRecordResponse>>
                ) {
                    if (!res.isSuccessful || res.body().isNullOrEmpty()) return

                    val list = res.body()!!

                    val entries = list.mapIndexed { i, rec ->
                        BarEntry(i.toFloat(), rec.calories.toFloat())
                    }

                    val dataSet = BarDataSet(entries, "일일 섭취 칼로리")
                    dataSet.color = resources.getColor(R.color.teal_700)

                    val barData = BarData(dataSet)
                    barData.barWidth = 0.4f

                    barChartWeekly.data = barData
                    barChartWeekly.description.isEnabled = false
                    barChartWeekly.invalidate()
                }

                override fun onFailure(call: Call<List<MealRecordResponse>>, t: Throwable) {}
            })
    }

    // ───────────────────────────────────────────────
    // ⚖️ 2) 주간 체중 변화 선 그래프
    // ───────────────────────────────────────────────
    private fun loadWeeklyWeight() {
        RetrofitClient.api.getWeeklyWeight()
            .enqueue(object : Callback<WeeklyWeightResponse> {
                override fun onResponse(
                    call: Call<WeeklyWeightResponse>,
                    res: Response<WeeklyWeightResponse>
                ) {
                    if (!res.isSuccessful || res.body() == null) return

                    val items = res.body()!!.weekly_weight

                    // weight == null인 날은 그래프에 표시하지 않음
                    val entries = items.mapIndexedNotNull { index, item ->
                        item.weight?.let {
                            Entry(index.toFloat(), it)
                        }
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
}
