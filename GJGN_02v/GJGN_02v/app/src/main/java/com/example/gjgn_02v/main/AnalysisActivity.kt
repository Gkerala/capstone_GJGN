package com.example.gjgn_02v.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.analysis.WeeklyAnalysisResponse
import com.example.gjgn_02v.data.model.goals.WeeklyWeightResponse
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AnalysisActivity : AppCompatActivity() {

    private lateinit var barChartWeekly: BarChart
    private lateinit var lineChartWeight: LineChart
    private lateinit var tvWeekRange: TextView

    private lateinit var btnPrevWeek: ImageView
    private lateinit var btnNextWeek: ImageView

    private val calendar: Calendar = Calendar.getInstance()

    /** X축 요일 라벨 */
    private val dayLabels = arrayOf("월", "화", "수", "목", "금", "토", "일")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        barChartWeekly = findViewById(R.id.barChartWeekly)
        lineChartWeight = findViewById(R.id.lineChartWeight)
        tvWeekRange = findViewById(R.id.tvWeekRange)

        btnPrevWeek = findViewById(R.id.btnPrevWeek)
        btnNextWeek = findViewById(R.id.btnNextWeek)

        setupCharts()
        setupWeekSelector()
        loadWeeklyData()
        setupBottomNav()
    }

    // ============================================================
    // 📌 공통 축 스타일 Preset
    // ============================================================
    private fun applyCommonChartStyle(xAxis: XAxis) {
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels)
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 10f
        xAxis.yOffset = 6f
        xAxis.labelRotationAngle = -10f
    }
    private fun applyChartOffsets() {
        barChartWeekly.setExtraOffsets(0f, 0f, 0f, 14f)
        lineChartWeight.setExtraOffsets(0f, 0f, 0f, 14f)
    }

    private fun setupCharts() {
        barChartWeekly.xAxis.apply { applyCommonChartStyle(this) }
        barChartWeekly.axisRight.isEnabled = false
        barChartWeekly.description.isEnabled = false

        lineChartWeight.xAxis.apply { applyCommonChartStyle(this) }
        lineChartWeight.axisRight.isEnabled = false
        lineChartWeight.description.isEnabled = false

        applyChartOffsets()   // 🔥 이제 두 그래프가 같은 위치에서 X축 시작
    }

    private fun loadWeeklyData() {
        loadWeeklyCalories()
        loadWeeklyWeight()
    }


    // ============================================================
    // 📌 주 단위 날짜 계산
    // ============================================================
    private fun getCurrentMonday(): Calendar {
        val cal = calendar.clone() as Calendar
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val diff = if (dayOfWeek == Calendar.SUNDAY) -6 else (Calendar.MONDAY - dayOfWeek)
        cal.add(Calendar.DAY_OF_MONTH, diff)
        return cal
    }

    private fun getCurrentSunday(): Calendar =
        (getCurrentMonday().clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 6) }

    private fun updateWeekRangeText() {
        val monday = getCurrentMonday()
        val sunday = getCurrentSunday()
        val sdf = SimpleDateFormat("MM.dd", Locale.KOREA)
        tvWeekRange.text = "${sdf.format(monday.time)} ~ ${sdf.format(sunday.time)}"
    }

    private fun setupWeekSelector() {
        updateWeekRangeText()

        btnPrevWeek.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeekRangeText()
            loadWeeklyData()
        }

        btnNextWeek.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeekRangeText()
            loadWeeklyData()
        }
    }

    private fun getQueryDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        return sdf.format(calendar.time)
    }

    // ============================================================
    // 📌 축 범위 계산 공통 함수
    // ============================================================
    private fun calculateAxisRange(
        values: List<Float>,
        extraTop: Float,
        extraBottom: Float
    ): Pair<Float, Float> {

        val realValues = values.filter { !it.isNaN() }

        if (realValues.isEmpty()) return 0f to 100f

        val minVal = realValues.minOrNull() ?: 0f
        val maxVal = realValues.maxOrNull() ?: 0f

        val minAxis = maxOf(0f, minVal - extraBottom)
        val maxAxis = maxVal + extraTop

        return minAxis to maxAxis
    }

    // ============================================================
    // 📊 주간 칼로리
    // ============================================================
    private fun loadWeeklyCalories() {
        RetrofitClient.api.getWeeklyAnalysis(getQueryDate())
            .enqueue(object : Callback<WeeklyAnalysisResponse> {
                override fun onResponse(
                    call: Call<WeeklyAnalysisResponse>,
                    res: Response<WeeklyAnalysisResponse>
                ) {

                    val rawList = res.body()?.weekly_records ?: emptyList()
                    val list = MutableList(7) { idx -> rawList.getOrNull(idx) }

                    val values = list.map { it?.calories?.toFloat() ?: Float.NaN }

                    val entries = values.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }

                    val dataSet = BarDataSet(entries, "주간 칼로리").apply {
                        color = Color.parseColor("#FF9800")

                        // 🔢 막대 위에 칼로리 숫자 표시
                        setDrawValues(true)
                        valueTextSize = 10f
                        valueTextColor = getColor(R.color.black)
                    }

                    barChartWeekly.data = BarData(dataSet).apply {
                        barWidth = 0.4f
                    }

                    // 🔥 Y축 계산
                    val realValues = values.filter { !it.isNaN() }

                    val maxAxis = if (realValues.isNotEmpty()) {
                        realValues.maxOrNull()!! + 50f
                    } else {
                        100f
                    }

                    barChartWeekly.axisLeft.apply {
                        axisMinimum = 0f     // 🔥 요구사항: 최소값 무조건 0
                        axisMaximum = maxAxis
                    }

                    barChartWeekly.invalidate()
                }

                override fun onFailure(call: Call<WeeklyAnalysisResponse>, t: Throwable) {

                    val emptyEntries = (0 until 7).map { BarEntry(it.toFloat(), Float.NaN) }
                    val dataSet = BarDataSet(emptyEntries, "주간 칼로리")

                    barChartWeekly.data = BarData(dataSet)

                    barChartWeekly.axisLeft.apply {
                        axisMinimum = 0f
                        axisMaximum = 100f
                    }

                    barChartWeekly.invalidate()
                }
            })
    }


    // ============================================================
    // ⚖ 주간 체중
    // ============================================================
    private fun loadWeeklyWeight() {
        RetrofitClient.api.getWeeklyWeight(getQueryDate())
            .enqueue(object : Callback<WeeklyWeightResponse> {
                override fun onResponse(
                    call: Call<WeeklyWeightResponse>,
                    res: Response<WeeklyWeightResponse>
                ) {

                    val rawList = res.body()?.records ?: emptyList()
                    val list = MutableList(7) { idx -> rawList.getOrNull(idx) }

                    val values = list.map { it?.weight?.toFloat() ?: Float.NaN }

                    val entries = values.mapIndexed { i, v -> Entry(i.toFloat(), v) }

                    val dataSet = LineDataSet(entries, "체중 변화 (kg)").apply {

                        color = Color.parseColor("#FF9800")
                        lineWidth = 3f

                        // 🔵 데이터 있는 부분만 원 표시
                        setDrawCircles(true)
                        circleRadius = 4f
                        setCircleColor(Color.parseColor("#FF9800"))
                        setDrawCircleHole(false)

                        // 🔢 데이터 값 표시 (NaN은 표시 안됨)
                        setDrawValues(true)
                        valueTextSize = 9f
                        valueTextColor = getColor(R.color.black)

                        // 부드러운 선 or 직선
                        mode = LineDataSet.Mode.LINEAR
                    }

                    lineChartWeight.data = LineData(dataSet)

                    // 축 계산
                    val (minAxis, maxAxis) = calculateAxisRange(
                        values,
                        extraTop = 1f,
                        extraBottom = 1f
                    )

                    lineChartWeight.axisLeft.apply {
                        axisMinimum = minAxis
                        axisMaximum = maxAxis
                    }

                    lineChartWeight.invalidate()
                }

                override fun onFailure(call: Call<WeeklyWeightResponse>, t: Throwable) {

                    val emptyEntries = (0 until 7).map { Entry(it.toFloat(), Float.NaN) }
                    val dataSet = LineDataSet(emptyEntries, "체중 변화 (kg)")

                    lineChartWeight.data = LineData(dataSet)

                    lineChartWeight.axisLeft.apply {
                        axisMinimum = 0f
                        axisMaximum = 100f
                    }

                    lineChartWeight.invalidate()
                }
            })
    }

    // ============================================================
    // ⬇ 하단 네비게이션
    // ============================================================
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        bottomNav.selectedItemId = R.id.menu_analysis

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> startActivity(Intent(this, MainActivity::class.java))
                R.id.menu_record -> startActivity(Intent(this, RecordSelectActivity::class.java))
                R.id.menu_analysis -> return@setOnItemSelectedListener true
                R.id.menu_mypage ->
                    startActivity(Intent(this, MyPageActivity::class.java))
            }
            true
        }
    }
}
