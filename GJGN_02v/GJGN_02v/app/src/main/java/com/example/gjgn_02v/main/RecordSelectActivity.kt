package com.example.gjgn_02v.main

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.records.MealDayResponse
import com.example.gjgn_02v.data.model.records.MealItem
import com.example.gjgn_02v.data.model.goals.WeightResponse
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecordSelectActivity : AppCompatActivity() {

    private lateinit var containerMorning: LinearLayout
    private lateinit var containerLunch: LinearLayout
    private lateinit var containerDinner: LinearLayout

    private lateinit var btnAddMorning: Button
    private lateinit var btnAddLunch: Button
    private lateinit var btnAddDinner: Button

    private lateinit var txtWeightSummary: TextView
    private lateinit var btnGoWeightRecord: Button

    private lateinit var tvTotalKcal: TextView
    private lateinit var pieSummaryMacro: PieChart


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_select)

        setupBottomNav()
        initViews()
        setupButtons()

        loadTodayMeals()
        loadTodayWeight()
    }

    private fun initViews() {
        containerMorning = findViewById(R.id.containerMorning)
        containerLunch = findViewById(R.id.containerLunch)
        containerDinner = findViewById(R.id.containerDinner)

        btnAddMorning = findViewById(R.id.btnAddMorning)
        btnAddLunch = findViewById(R.id.btnAddLunch)
        btnAddDinner = findViewById(R.id.btnAddDinner)

        txtWeightSummary = findViewById(R.id.txtWeightSummary)
        btnGoWeightRecord = findViewById(R.id.btnGoWeightRecord)
        tvTotalKcal = findViewById(R.id.tvTotalKcal)
        pieSummaryMacro = findViewById(R.id.pieSummaryMacro)

    }

    private fun setupButtons() {
        btnAddMorning.setOnClickListener { openMealRecord("breakfast") }
        btnAddLunch.setOnClickListener { openMealRecord("lunch") }
        btnAddDinner.setOnClickListener { openMealRecord("dinner") }

        btnGoWeightRecord.setOnClickListener {
            startActivity(Intent(this, WeightRecordActivity::class.java))
        }
    }


    private fun openMealRecord(type: String) {
        val intent = Intent(this, MealRecordActivity::class.java)
        intent.putExtra("mealType", type)
        startActivity(intent)
    }

    /** ---------------------------
     *  오늘의 식단 불러오기
     * --------------------------- */
    private fun loadTodayMeals() {
        RetrofitClient.api.getTodayMeals().enqueue(object : Callback<MealDayResponse> {
            override fun onResponse(call: Call<MealDayResponse>, response: Response<MealDayResponse>) {
                if (!response.isSuccessful) return
                val data = response.body() ?: return

                containerMorning.removeAllViews()
                containerLunch.removeAllViews()
                containerDinner.removeAllViews()

                data.breakfast.forEach { item -> addMealCard(containerMorning, item) }
                data.lunch.forEach { item -> addMealCard(containerLunch, item) }
                data.dinner.forEach { item -> addMealCard(containerDinner, item) }

                /** --------------------------
                 *  🔥 총칼로리 + 탄단지 계산
                 * -------------------------- */
                var totalKcal = 0.0
                var totalCarbs = 0.0
                var totalProtein = 0.0
                var totalFat = 0.0

                val all = data.breakfast + data.lunch + data.dinner
                all.forEach {
                    totalKcal += it.kcal
                    totalCarbs += it.carbs
                    totalProtein += it.protein
                    totalFat += it.fat
                }

                // 총 칼로리 UI 적용
                tvTotalKcal.text = "총 섭취: ${"%.1f".format(totalKcal)} kcal"

                // 원그래프 표시
                setPieChart(totalCarbs, totalProtein, totalFat)
            }

            override fun onFailure(call: Call<MealDayResponse>, t: Throwable) {}
        })
    }

    private fun setPieChart(carbs: Double, protein: Double, fat: Double) {

        val entries = ArrayList<PieEntry>()
        if (carbs > 0) entries.add(PieEntry(carbs.toFloat(), "탄수화물"))
        if (protein > 0) entries.add(PieEntry(protein.toFloat(), "단백질"))
        if (fat > 0) entries.add(PieEntry(fat.toFloat(), "지방"))

        val dataSet = PieDataSet(entries, "영양 비율")
        dataSet.sliceSpace = 3f
        dataSet.valueTextSize = 14f

        // 원그래프 색상 (원하는대로 변경 가능)
        dataSet.colors = listOf(
            Color.parseColor("#FFB74D"), // Carbs
            Color.parseColor("#81C784"), // Protein
            Color.parseColor("#E57373")  // Fat
        )

        val data = PieData(dataSet)

        pieSummaryMacro.data = data
        pieSummaryMacro.description.isEnabled = false
        pieSummaryMacro.setUsePercentValues(true)
        pieSummaryMacro.invalidate()
    }


    /** ---------------------------
     *   카드뷰 하나 추가
     * --------------------------- */
    private fun addMealCard(container: LinearLayout, item: MealItem) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.item_meal_food, container, false)

        val tvFoodName = view.findViewById<TextView>(R.id.tvFoodName)
        val tvFoodInfo = view.findViewById<TextView>(R.id.tvFoodInfo)
        val btnDeleteFood = view.findViewById<ImageButton>(R.id.btnDeleteFood)

        tvFoodName.text = item.name
        tvFoodInfo.text = "${item.kcal} kcal · 탄:${item.carbs}g / 단:${item.protein}g / 지:${item.fat}g"

        btnDeleteFood.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("삭제")
                .setMessage("${item.name} 삭제할까요?")
                .setPositiveButton("삭제") { _, _ ->
                    deleteMealItem(item.id)
                }
                .setNegativeButton("취소", null)
                .show()
        }

        container.addView(view)
    }

    /** ---------------------------
     *   삭제 요청
     * --------------------------- */
    private fun deleteMealItem(id: Int) {
        RetrofitClient.api.deleteMealItem(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RecordSelectActivity, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                    loadTodayMeals()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }

    /** ---------------------------
     *  오늘의 체중 불러오기
     * --------------------------- */
    private fun loadTodayWeight() {
        RetrofitClient.api.getTodayWeight().enqueue(object : Callback<WeightResponse?> {

            override fun onResponse(
                call: Call<WeightResponse?>,
                response: Response<WeightResponse?>
            ) {
                if (!response.isSuccessful) return
                val data = response.body() ?: return

                txtWeightSummary.text = "${data.weight} kg"
            }

            override fun onFailure(
                call: Call<WeightResponse?>,
                t: Throwable
            ) { }
        })
    }


    /** ---------------------------
     *  하단 네비게이션
     * --------------------------- */
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
