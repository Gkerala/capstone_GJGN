package com.example.gjgn_02v.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.WeightRequest
import com.example.gjgn_02v.data.model.goals.WeightResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecordSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_select)

        setupBottomNav()

        val btnMeal = findViewById<Button>(R.id.btnMealRecord)
        val btnWeight = findViewById<Button>(R.id.btnWeightRecord)

        btnMeal.setOnClickListener {
            startActivity(Intent(this, MealRecordActivity::class.java))
        }

        btnWeight.setOnClickListener {
            startActivity(Intent(this, WeightRecordActivity::class.java))
        }
    }

    /** 체중 등록 요청 (서버 전송) */
    private fun saveWeight(weight: Float) {

        val request = WeightRequest(weight)

        RetrofitClient.api.createWeight(request)
            .enqueue(object : Callback<WeightResponse> {
                override fun onResponse(
                    call: Call<WeightResponse>,
                    response: Response<WeightResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RecordSelectActivity,
                            "체중이 저장되었습니다!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@RecordSelectActivity,
                            "저장 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<WeightResponse>, t: Throwable) {
                    Toast.makeText(
                        this@RecordSelectActivity,
                        "서버 연결 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /** 하단 네비게이션 */
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_record

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.menu_record -> true
                R.id.menu_analysis -> {
                    startActivity(Intent(this, AnalysisActivity::class.java))
                    true
                }
                R.id.menu_mypage -> {
                    startActivity(Intent(this, MyPageActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
