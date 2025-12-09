package com.example.gjgn_02v.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.analysis.MainSummaryResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    // 오늘의 요약
    private lateinit var tvTodayCalorie: TextView
    private lateinit var tvCarb: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvFat: TextView
    private lateinit var tvSugar: TextView

    // 체중
    private lateinit var tvTodayWeight: TextView
    private lateinit var tvGoalWeight: TextView
    private lateinit var tvWeightDiff: TextView

    // 식단
    private lateinit var tvFoodBreakfast: TextView
    private lateinit var tvFoodLunch: TextView
    private lateinit var tvFoodDinner: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadMainSummary()
        setupBottomNav()
    }

    // ---------------------------------------------------------
    // 뷰 초기화
    // ---------------------------------------------------------
    private fun initViews() {

        // 오늘의 요약
        tvTodayCalorie = findViewById(R.id.tvTodayCalorie)
        tvCarb = findViewById(R.id.tvCarb)
        tvProtein = findViewById(R.id.tvProtein)
        tvFat = findViewById(R.id.tvFat)
        tvSugar = findViewById(R.id.tvSugar)

        // 체중
        tvTodayWeight = findViewById(R.id.tvTodayWeight)
        tvGoalWeight = findViewById(R.id.tvGoalWeight)
        tvWeightDiff = findViewById(R.id.tvWeightDiff)

        // 식단
        tvFoodBreakfast = findViewById(R.id.tvFoodBreakfast)
        tvFoodLunch = findViewById(R.id.tvFoodLunch)
        tvFoodDinner = findViewById(R.id.tvFoodDinner)
    }

    // ---------------------------------------------------------
    // 🔥 서버에서 메인요약 데이터 가져오기
    // ---------------------------------------------------------
    private fun loadMainSummary() {

        RetrofitClient.api.getMainSummary().enqueue(object : Callback<MainSummaryResponse> {
            override fun onResponse(
                call: Call<MainSummaryResponse>,
                response: Response<MainSummaryResponse>
            ) {

                val body = response.body() ?: return

                val t = body.today  // 오늘의 요약
                val w = body.weight // 체중
                val m = body.meals  // 식단

                // ----------------------------------------
                // 🔥 오늘의 영양소/칼로리
                // ----------------------------------------
                tvTodayCalorie.text =
                    "${t.total_kcal} / ${t.goal_kcal} kcal (${t.kcal_percent}%)"

                tvCarb.text = "탄수화물 ${t.carb} / ${t.goal_carb} g (${t.carb_percent}%)"
                tvProtein.text = "단백질 ${t.protein} / ${t.goal_protein} g (${t.protein_percent}%)"
                tvFat.text = "지방 ${t.fat} / ${t.goal_fat} g (${t.fat_percent}%)"
                tvSugar.text = "당 ${t.sugar} / ${t.goal_sugar} g (${t.sugar_percent}%)"


                // ----------------------------------------
                // 🔥 체중 정보
                // ----------------------------------------

                // 오늘 체중
                tvTodayWeight.text =
                    if (w.today_weight != null) "오늘 체중: ${w.today_weight} kg"
                    else "오늘 체중: 기록 없음"

                // 목표 체중
                tvGoalWeight.text =
                    if (w.goal_weight != null) "목표 체중: ${w.goal_weight} kg"
                    else "목표 체중: 설정 안됨"

                // 남은 체중 계산
                if (w.goal_weight != null && w.today_weight != null) {
                    val diff = w.today_weight - w.goal_weight
                    tvWeightDiff.text = "앞으로 ${"%.1f".format(diff)} kg"
                } else {
                    tvWeightDiff.text = "남은 체중: -"
                }


                // ----------------------------------------
                // 🔥 오늘의 식단
                // ----------------------------------------
                tvFoodBreakfast.text = if (m.breakfast != null) {
                    val list = m.breakfast.foods.joinToString { it.name }
                    "아침: $list"
                } else {
                    "아침: 없음"
                }

                tvFoodLunch.text = if (m.lunch != null) {
                    val list = m.lunch.foods.joinToString { it.name }
                    "점심: $list"
                } else {
                    "점심: 없음"
                }

                tvFoodDinner.text = if (m.dinner != null) {
                    val list = m.dinner.foods.joinToString { it.name }
                    "저녁: $list"
                } else {
                    "저녁: 없음"
                }
            }

            override fun onFailure(call: Call<MainSummaryResponse>, t: Throwable) {
                // 실패해도 앱이 죽지 않도록 기본값 표시
                tvTodayCalorie.text = "0 / 0 kcal"
                tvCarb.text = "탄수화물 0"
                tvProtein.text = "단백질 0"
                tvFat.text = "지방 0"
                tvSugar.text = "당 0"

                tvTodayWeight.text = "오늘 체중: -"
                tvGoalWeight.text = "목표 체중: -"
                tvWeightDiff.text = "남은 체중: -"

                tvFoodBreakfast.text = "아침: 없음"
                tvFoodLunch.text = "점심: 없음"
                tvFoodDinner.text = "저녁: 없음"
            }
        })
    }


    // ---------------------------------------------------------
    // 하단 네비게이션
    // ---------------------------------------------------------
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        bottomNav.selectedItemId = R.id.menu_main

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> return@setOnItemSelectedListener true
                R.id.menu_record -> startActivity(Intent(this, RecordSelectActivity::class.java))
                R.id.menu_analysis -> startActivity(Intent(this, AnalysisActivity::class.java))
                R.id.menu_mypage -> startActivity(Intent(this, MyPageActivity::class.java))
            }
            true
        }
    }
}
