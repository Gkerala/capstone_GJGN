package com.example.gjgn_02v.main

import android.os.Bundle
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.model.records.AppDatabase
import com.example.gjgn_02v.data.model.records.WeightEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WeightRecordActivity : AppCompatActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "smartdiet_db"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_record)

        /** ▼ NumberPicker 세팅 */
        val np100 = findViewById<NumberPicker>(R.id.np100)
        val np10 = findViewById<NumberPicker>(R.id.np10)
        val np1 = findViewById<NumberPicker>(R.id.np1)
        val npDecimal = findViewById<NumberPicker>(R.id.npDecimal)

        np100.minValue = 0; np100.maxValue = 2
        np10.minValue = 0; np10.maxValue = 9
        np1.minValue = 0; np1.maxValue = 9
        npDecimal.minValue = 0; npDecimal.maxValue = 9

        /** ▼ 등록 버튼 */
        findViewById<android.widget.Button>(R.id.btnSave).setOnClickListener {
            val weight = "${np100.value}${np10.value}${np1.value}.${npDecimal.value}".toFloat()

            saveWeight(weight)
        }
    }

    /** ▼ 체중 저장 후 이전 페이지로 돌아가기 */
    private fun saveWeight(weight: Float) {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        lifecycleScope.launch {
            db.weightDao().insert(
                WeightEntity(
                    weight = weight,
                    date = date
                )
            )

            Toast.makeText(
                this@WeightRecordActivity,
                "체중이 저장되었습니다!",
                Toast.LENGTH_SHORT
            ).show()

            finish()  // 저장 후 이전 페이지 이동
        }
    }

}
