package com.example.gjgn_02v.main

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnalysisActivity : AppCompatActivity() {

    private lateinit var txtWeekly: TextView
    private lateinit var txtMonthly: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        txtWeekly = findViewById(R.id.txt_weekly)
        txtMonthly = findViewById(R.id.txt_monthly)

        loadWeekly()
        loadMonthly()
    }

    private fun loadWeekly() {
        RetrofitClient.api.getWeeklyAchievement()
            .enqueue(object : Callback<GoalStatResponse> {
                override fun onResponse(call: Call<GoalStatResponse>, res: Response<GoalStatResponse>) {
                    if (res.isSuccessful) {
                        txtWeekly.text = "주간 달성률: ${res.body()!!.achievement}%"
                    }
                }
                override fun onFailure(call: Call<GoalStatResponse>, t: Throwable) {}
            })
    }

    private fun loadMonthly() {
        RetrofitClient.api.getMonthlyAchievement()
            .enqueue(object : Callback<GoalStatResponse> {
                override fun onResponse(call: Call<GoalStatResponse>, res: Response<GoalStatResponse>) {
                    if (res.isSuccessful) {
                        txtMonthly.text = "월간 달성률: ${res.body()!!.achievement}%"
                    }
                }
                override fun onFailure(call: Call<GoalStatResponse>, t: Throwable) {}
            })
    }
}
