package com.example.gjgn_02v.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.model.records.AppDatabase
import com.example.gjgn_02v.data.model.records.WeightEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RecordSelectActivity : AppCompatActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "smartdiet_db"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_select)

        setupBottomNav()

        val btnMeal = findViewById<Button>(R.id.btnMealRecord)
        val btnWeight = findViewById<Button>(R.id.btnWeightRecord)

        btnMeal.setOnClickListener {
            startActivity(Intent(this, MealRecordActivity::class.java))
        }

        // 체중 기록 페이지로 이동
        btnWeight.setOnClickListener {
            startActivity(Intent(this, WeightRecordActivity::class.java))
        }
    }


    /** 체중 입력 다이얼로그 */
    private fun openWeightInputDialog() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_weight_input, null)

        val np100 = dialogView.findViewById<NumberPicker>(R.id.np100)
        val np10 = dialogView.findViewById<NumberPicker>(R.id.np10)
        val np1 = dialogView.findViewById<NumberPicker>(R.id.np1)
        val npDecimal = dialogView.findViewById<NumberPicker>(R.id.npDecimal)

        // Picker 설정
        np100.minValue = 0; np100.maxValue = 2
        np10.minValue = 0; np10.maxValue = 9
        np1.minValue = 0; np1.maxValue = 9
        npDecimal.minValue = 0; npDecimal.maxValue = 9

        AlertDialog.Builder(this)
            .setTitle("체중 입력")
            .setView(dialogView)
            .setPositiveButton("등록") { _, _ ->

                val weight = "${np100.value}${np10.value}${np1.value}.${npDecimal.value}".toFloat()

                saveWeight(weight)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /** DB에 체중 저장 */
    private fun saveWeight(weight: Float) {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        lifecycleScope.launch {
            db.weightDao().insert(
                WeightEntity(
                    weight = weight,
                    date = date
                )
            )

            // 저장 후 체중 기록 목록 페이지로 이동
            startActivity(Intent(this@RecordSelectActivity, WeightRecordActivity::class.java))
        }
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
