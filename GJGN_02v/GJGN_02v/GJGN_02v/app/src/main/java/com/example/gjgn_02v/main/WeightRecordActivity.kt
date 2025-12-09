package com.example.gjgn_02v.main

import android.os.Bundle
import android.widget.NumberPicker
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.WeightRequest
import com.example.gjgn_02v.data.model.goals.WeightResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Button

class WeightRecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_record)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val np100 = findViewById<NumberPicker>(R.id.np100)
        val np10 = findViewById<NumberPicker>(R.id.np10)
        val np1 = findViewById<NumberPicker>(R.id.np1)
        val npDecimal = findViewById<NumberPicker>(R.id.npDecimal)

        np100.minValue = 0; np100.maxValue = 2
        np10.minValue = 0; np10.maxValue = 9
        np1.minValue = 0; np1.maxValue = 9
        npDecimal.minValue = 0; npDecimal.maxValue = 9

        findViewById<android.widget.Button>(R.id.btnSave).setOnClickListener {
            val weight = "${np100.value}${np10.value}${np1.value}.${npDecimal.value}".toFloat()

            // 🔥 서버에 체중 저장
            saveWeight(weight)
        }
    }

    /** ▼ 서버에 체중 저장 (saveWeight 그대로 적용) */
    private fun saveWeight(weight: Float) {

        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        val request = WeightRequest(
            weight = weight,
            date = today
        )

        RetrofitClient.api.createWeight(request)
            .enqueue(object : Callback<WeightResponse> {
                override fun onResponse(
                    call: Call<WeightResponse>,
                    response: Response<WeightResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@WeightRecordActivity,
                            "체중 기록 저장 완료!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@WeightRecordActivity,
                            "저장 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<WeightResponse>, t: Throwable) {
                    Toast.makeText(
                        this@WeightRecordActivity,
                        "서버 연결 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


}
