package com.example.gjgn_02v.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.GoalStatResponse
import com.example.gjgn_02v.data.model.records.MealRecordResponse
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
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

    private fun loadWeeklyWeight() {
        RetrofitClient.api.getWeeklyAchievement()
            .enqueue(object : Callback<GoalStatResponse> {
                override fun onResponse(
                    call: Call<GoalStatResponse>,
                    res: Response<GoalStatResponse>
                ) {
                    if (!res.isSuccessful || res.body() == null) return

                    val weightList = res.body()!!.weights

                    val entries = weightList.mapIndexed { i, w ->
                        Entry(i.toFloat(), w.toFloat())
                    }

                    val set = LineDataSet(entries, "체중 변화 (kg)")
                    set.lineWidth = 3f
                    set.color = resources.getColor(R.color.purple_700)
                    set.circleRadius = 4f

                    lineChartWeight.data = LineData(set)
                    lineChartWeight.description = Description().apply { text = "" }
                    lineChartWeight.invalidate()
                }

                override fun onFailure(call: Call<GoalStatResponse>, t: Throwable) {}
            })
    }
}
