package com.example.gjgn_02v.main

import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.foods.AiFoodDetectResponse
import com.example.gjgn_02v.data.model.foods.FoodItemResponse
import com.example.gjgn_02v.data.model.foods.FoodSearchResponse
import com.example.gjgn_02v.data.model.foods.NutritionResponse
import com.example.gjgn_02v.data.model.records.MealRecordRequest
import com.example.gjgn_02v.data.model.records.MealRecordResponse
import com.example.gjgn_02v.data.model.records.FoodData
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class MealRecordActivity : AppCompatActivity() {

    private lateinit var btnBreakfast: Button
    private lateinit var btnLunch: Button
    private lateinit var btnDinner: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var btnSelectImage: Button
    private lateinit var searchInput: EditText
    private lateinit var imgPreview: ImageView
    private lateinit var analysisContainer: LinearLayout
    private lateinit var btnSave: Button

    private var selectedFood: FoodItemResponse? = null
    private var singleNutritionResult: NutritionResponse? = null

    private var nutritionList = mutableListOf<NutritionResponse>()
    private var selectedMealType = "breakfast"
    private var foods = listOf<FoodItemResponse>()

    private var selectedUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private var currentPhotoPath: String = ""

    private var yoloFoods: List<String> = emptyList()
    private val CAMERA_REQUEST_CODE = 1001

    companion object {
        private const val PICK_IMAGE = 2001
        private const val CAMERA_REQUEST = 3001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_record)
        yoloFoods = loadYoloNames()

        initViews()
        setupMealButtons()
        applyMealTypeAutoSelect()
        setupSearchInput()
        setupBottomNav()

        btnTakePhoto.setOnClickListener { openCamera() }
        btnSelectImage.setOnClickListener { pickImageFromGallery() }
        btnSave.setOnClickListener { saveRecord() }

        val btnSearch = findViewById<Button>(R.id.btnSearchFood)

        btnSearch.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                val mapped = mapFoodName(query)
                manualSearch(mapped)
            } else {
                Toast.makeText(this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()  // 현재 Activity 종료 -> 이전 화면으로 이동
        }

    }

    private val koreanToEnglishMap = mapOf(

        // cake
        "케이크" to "cake",
        "케익" to "cake",
        "초코케이크" to "cake",

        // french fries
        "감자튀김" to "french",
        "프렌치프라이" to "french",
        "후렌치후라이" to "french",

        // hamburger
        "햄버거" to "hamburger",
        "버거" to "hamburger",

        // hot dog
        "핫도그" to "hot",
        "핫도그빵" to "hot",

        // pasta
        "파스타" to "pasta",
        "스파게티" to "pasta",

        // pizza
        "피자" to "pizza",
        "치즈피자" to "pizza",
        "페퍼로니피자" to "pizza",

        // salad
        "샐러드" to "salad",
        "사라다" to "salad",

        // sandwich
        "샌드위치" to "sandwich",
        "샌드" to "sandwich",

        // sushi
        "스시" to "sushi",
        "초밥" to "sushi",
        "참치초밥" to "sushi",

        // taco
        "타코" to "taco",
        "타코요리" to "taco"
    )


    private fun mapFoodName(name: String): String {
        return koreanToEnglishMap[name] ?: name
    }
    private fun initViews() {
        btnBreakfast = findViewById(R.id.btnBreakfast)
        btnLunch = findViewById(R.id.btnLunch)
        btnDinner = findViewById(R.id.btnDinner)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        searchInput = findViewById(R.id.inputSearch)
        imgPreview = findViewById(R.id.imgPreview)
        analysisContainer = findViewById(R.id.analysisContainer)
        btnSave = findViewById(R.id.btnSave)

        analysisContainer.visibility = View.GONE
    }

    private fun loadYoloNames(): List<String> {
        val input = assets.open("data.yaml")
        val yaml = input.bufferedReader().use { it.readText() }

        val lines = yaml.lines()

        val namesStartIndex = lines.indexOfFirst { it.trim().startsWith("names:") }
        if (namesStartIndex == -1) return emptyList()

        val result = mutableListOf<String>()

        for (i in namesStartIndex + 1 until lines.size) {
            val line = lines[i].trim()
            if (!line.startsWith("-")) break

            val item = line.removePrefix("-").trim()
            if (item.isNotEmpty()) {

                // “sushi / 초밥 / 스시” 분리
                val tokens = item.split("/").map { it.trim() }.filter { it.isNotEmpty() }

                result.addAll(tokens)  // 자동완성으로 사용될 이름들
            }
        }

        return result
    }




    // ------------------------------------------------------------
    // 1. Meal type buttons
    // ------------------------------------------------------------
    private fun setupMealButtons() {
        val buttons = listOf(btnBreakfast, btnLunch, btnDinner)

        fun focus(btn: Button) {
            buttons.forEach {
                it.setBackgroundColor(getColor(R.color.gray_light))
                it.setTextColor(getColor(R.color.black))
            }
            btn.setBackgroundColor(getColor(R.color.teal_700))
            btn.setTextColor(getColor(android.R.color.white))
        }

        btnBreakfast.setOnClickListener { selectedMealType = "breakfast"; focus(btnBreakfast) }
        btnLunch.setOnClickListener { selectedMealType = "lunch"; focus(btnLunch) }
        btnDinner.setOnClickListener { selectedMealType = "dinner"; focus(btnDinner) }

        focus(btnBreakfast)
    }

    private fun applyMealTypeAutoSelect() {
        val type = intent.getStringExtra("mealType") ?: "breakfast"

        selectedMealType = type

        when (type) {
            "breakfast" -> btnBreakfast.performClick()
            "lunch" -> btnLunch.performClick()
            "dinner" -> btnDinner.performClick()
        }
    }

    // ------------------------------------------------------------
    // 2. 음식 검색 기능
    // ------------------------------------------------------------
    private fun setupSearchInput() {

        // 키보드 Enter → 직접 검색 실행
        searchInput.setOnEditorActionListener { _, _, _ ->
            val q = searchInput.text.toString().trim()
            if (q.isNotEmpty()) manualSearch(mapFoodName(q))
            true
        }

        searchInput.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim()
                val searchContainer = findViewById<LinearLayout>(R.id.searchResultContainer)

                if (q.isEmpty()) {
                    searchContainer.visibility = View.GONE
                    searchContainer.removeAllViews()
                    return
                }

                // YOLO names 자동완성 필터링
                val filtered = yoloFoods.filter { it.contains(q, ignoreCase = true) }

                if (filtered.isEmpty()) {
                    searchContainer.visibility = View.GONE
                    searchContainer.removeAllViews()
                    return
                }

                searchContainer.visibility = View.VISIBLE
                searchContainer.removeAllViews()

                for (name in filtered) {
                    val tv = TextView(this@MealRecordActivity).apply {
                        text = name
                        textSize = 16f
                        setPadding(30, 25, 30, 25)
                        setBackgroundResource(android.R.color.white)

                        setOnClickListener {
                            searchInput.setText(name)
                            searchContainer.visibility = View.GONE
                            manualSearch(mapFoodName(name))
                        }
                    }
                    searchContainer.addView(tv)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }



    private fun manualSearch(foodName: String) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getNutrition(foodName)
                if (!res.isSuccessful || res.body() == null) {
                    Toast.makeText(this@MealRecordActivity, "검색된 영양정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                loadSingleNutrition(foodName)
            } catch (e: Exception) {
                Toast.makeText(this@MealRecordActivity, "검색 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun searchFoods(query: String) {
        RetrofitClient.api.searchFoods(query)
            .enqueue(object : Callback<FoodSearchResponse> {

                override fun onResponse(
                    call: Call<FoodSearchResponse>,
                    res: Response<FoodSearchResponse>
                ) {
                    val searchContainer = findViewById<LinearLayout>(R.id.searchResultContainer)
                    searchContainer.removeAllViews()

                    if (!res.isSuccessful) {
                        searchContainer.visibility = View.GONE
                        return
                    }

                    val body = res.body() ?: return

                    if (body.results.isEmpty()) {
                        searchContainer.visibility = View.GONE
                        return
                    }

                    foods = body.results
                    searchContainer.visibility = View.VISIBLE

                    for (item in foods) {
                        val tv = TextView(this@MealRecordActivity).apply {
                            text = item.name
                            textSize = 16f
                            setPadding(30, 25, 30, 25)
                            setBackgroundResource(android.R.color.white)

                            setOnClickListener {
                                selectedFood = item
                                selectedUri = null
                                imgPreview.visibility = View.GONE
                                loadSingleNutrition(item.name)
                                searchContainer.visibility = View.GONE
                            }
                        }
                        searchContainer.addView(tv)
                    }
                }

                override fun onFailure(call: Call<FoodSearchResponse>, t: Throwable) {}
            })
    }

    // ------------------------------------------------------------
    // 3. 이미지 선택
    // ------------------------------------------------------------
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    // ------------------------------------------------------------
    // onActivityResult
    // ------------------------------------------------------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 갤러리 선택
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedUri = data?.data ?: return
            selectedFood = null
            singleNutritionResult = null

            imgPreview.setImageURI(selectedUri)
            imgPreview.visibility = View.VISIBLE

            nutritionList.clear()
            analysisContainer.removeAllViews()
            analysisContainer.visibility = View.VISIBLE

            uploadImage(selectedUri!!)
        }

        // 카메라 촬영
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            cameraImageUri?.let { uri ->
                selectedUri = uri

                imgPreview.setImageURI(uri)
                imgPreview.visibility = View.VISIBLE

                selectedFood = null
                singleNutritionResult = null
                nutritionList.clear()
                analysisContainer.removeAllViews()

                uploadImage(uri)
            }
        }
    }

    // ------------------------------------------------------------
    // 4. YOLO 이미지 분석
    // ------------------------------------------------------------
    private fun uploadImage(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val bytes = inputStream.readBytes()
        inputStream.close()

        val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val multipart = MultipartBody.Part.createFormData("image", "upload.jpg", reqFile)

        RetrofitClient.api.detectFood(multipart)
            .enqueue(object : Callback<AiFoodDetectResponse> {

                override fun onResponse(
                    call: Call<AiFoodDetectResponse>,
                    res: Response<AiFoodDetectResponse>
                ) {
                    if (!res.isSuccessful) return

                    val result = res.body() ?: return
                    if (result.foods.isEmpty()) {
                        Toast.makeText(this@MealRecordActivity, "음식을 감지하지 못했습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    fetchNutritionForFoods(result.foods.map { it.name })
                }

                override fun onFailure(call: Call<AiFoodDetectResponse>, t: Throwable) {}
            })
    }

    // ------------------------------------------------------------
    // 5. 여러 음식 Nutrition 조회
    // ------------------------------------------------------------
    private fun fetchNutritionForFoods(names: List<String>) {
        lifecycleScope.launch {
            nutritionList.clear()
            analysisContainer.removeAllViews()

            for (name in names) {
                try {
                    val res = RetrofitClient.api.getNutrition(name)
                    if (res.isSuccessful && res.body() != null) {
                        val n = res.body()!!
                        n.selected = true       // ★ 자동 선택
                        nutritionList.add(n)
                    } else {
                        Log.d("DBG_NUT", "getNutrition not successful for $name : code=${res.code()} body=${res.errorBody()}")
                    }
                } catch (e: Exception) {
                    Log.e("DBG_NUT", "exception getNutrition for $name", e)
                }
            }

            Log.d("DBG_NUT", "fetchNutritionForFoods finished, nutritionList.size=${nutritionList.size}")
            // force update on UI thread
            runOnUiThread { updateNutritionUI() }
        }
    }


    // ------------------------------------------------------------
    // 6. Nutrition UI 표시
    // ------------------------------------------------------------
    private fun updateNutritionUI() {

        nutritionList = nutritionList.distinctBy { it.name }.toMutableList()

        analysisContainer.visibility = View.VISIBLE
        analysisContainer.removeAllViews()

        nutritionList.forEachIndexed { idx, item ->
            val view = layoutInflater.inflate(R.layout.item_food_analysis, analysisContainer, false)

            val txtName = view.findViewById<TextView>(R.id.txtName)
            val txtGrams = view.findViewById<TextView>(R.id.txtGrams)
            val txtKcal = view.findViewById<TextView>(R.id.txtKcal)
            val txtCarbs = view.findViewById<TextView>(R.id.txtCarbs)
            val txtProtein = view.findViewById<TextView>(R.id.txtProtein)
            val txtFat = view.findViewById<TextView>(R.id.txtFat)
            val txtSugar = view.findViewById<TextView>(R.id.txtSugar)

            val btnEdit = view.findViewById<Button>(R.id.btnEdit)
            val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)

            val pieChart = view.findViewById<PieChart>(R.id.pieChartMacro)

            val txtPercentCarb = view.findViewById<TextView>(R.id.txtPercentCarb)
            val txtPercentProtein = view.findViewById<TextView>(R.id.txtPercentProtein)
            val txtPercentFat = view.findViewById<TextView>(R.id.txtPercentFat)

            // ---------------------------
            // 텍스트 세팅
            // ---------------------------
            txtName.text = item.name ?: "-"
            txtGrams.text = "${item.grams ?: 100f} g"
            txtKcal.text = "${item.calories ?: 0f} kcal"
            txtCarbs.text = "탄수화물: ${item.carbs ?: 0f}"
            txtProtein.text = "단백질: ${item.protein ?: 0f}"
            txtFat.text = "지방: ${item.fat ?: 0f}"
            txtSugar.text = "당: ${item.sugar ?: 0f}"

            // ---------------------------
            // 파이 차트 데이터
            // ---------------------------
            val carbs = item.carbs ?: 0f
            val protein = item.protein ?: 0f
            val fat = item.fat ?: 0f

            val entries = listOf(
                PieEntry(carbs, "탄"),
                PieEntry(protein, "단"),
                PieEntry(fat, "지")
            )

            val dataSet = PieDataSet(entries, "").apply {
                sliceSpace = 2f
                setDrawValues(false)
                colors = listOf(
                    Color.parseColor("#42A5F5"), // 탄
                    Color.parseColor("#66BB6A"), // 단
                    Color.parseColor("#FF7043")  // 지
                )
            }

            pieChart.apply {
                data = PieData(dataSet)
                holeRadius = 40f
                transparentCircleRadius = 45f
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(false)
                setDrawEntryLabels(false)
                invalidate()
            }

            // ---------------------------
            // 퍼센트 계산 + 표시
            // ---------------------------
            val total = carbs + protein + fat
            val safeTotal = if (total <= 0f) 1f else total

            fun pct(v: Float) = if (v <= 0f) "0%" else "${String.format("%.1f", v / safeTotal * 100f)}%"

            txtPercentCarb.text = "탄: ${pct(carbs)}"
            txtPercentProtein.text = "단: ${pct(protein)}"
            txtPercentFat.text = "지: ${pct(fat)}"

            // ---------------------------
            // 수정 버튼
            // ---------------------------
            btnEdit.setOnClickListener {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("섭취량(g) 수정")

                val input = EditText(this)
                input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                input.setText((item.grams ?: 100f).toInt().toString())
                dialog.setView(input)

                dialog.setPositiveButton("변경") { _, _ ->
                    val newGram = input.text.toString().toIntOrNull()
                    if (newGram != null && newGram > 0) {
                        val updated = scaleNutrition(item, newGram)
                        nutritionList[idx] = updated
                        updateNutritionUI()
                    }
                }

                dialog.setNegativeButton("취소", null)
                dialog.show()
            }

            // ---------------------------
            // 삭제 버튼
            // ---------------------------
            btnDelete.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("삭제 확인")
                    .setMessage("해당 음식 항목을 삭제할까요?")
                    .setPositiveButton("삭제") { _, _ ->
                        nutritionList.removeAt(idx)
                        updateNutritionUI()
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }

            analysisContainer.addView(view)
        }
    }





    // ------------------------------------------------------------
    // 7. 단일 Nutrition 조회
    // ------------------------------------------------------------
    private fun loadSingleNutrition(foodName: String) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getNutrition(foodName)
                if (!res.isSuccessful || res.body() == null) return@launch

                val n = res.body()!!
                singleNutritionResult = n

                // ① 검색 결과도 리스트에 저장
                nutritionList.clear()
                n.selected = true
                nutritionList.add(n)

                // ② UI 표시 허용
                analysisContainer.visibility = View.VISIBLE

                // ③ 기존 분석 UI 로직으로 렌더링 (수정 버튼 포함)
                runOnUiThread { updateNutritionUI() }

            } catch (_: Exception) { }
        }
    }

    private fun updateMacroPieChart(pieChart: PieChart, carbs: Float, protein: Float, fat: Float) {

        val total = carbs + protein + fat
        if (total <= 0f) return     // 값이 없으면 그래프 숨김 등 처리 가능

        val entries = listOf(
            PieEntry(carbs, "탄"),
            PieEntry(protein, "단"),
            PieEntry(fat, "지")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#42A5F5"),   // 탄: 파란색
                Color.parseColor("#66BB6A"),   // 단: 초록색
                Color.parseColor("#FF7043")    // 지: 주황색
            )
            valueTextColor = Color.BLACK
            valueTextSize = 10f
            sliceSpace = 2f
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            setUsePercentValues(true)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(10f)
            invalidate()
        }
    }



    // ------------------------------------------------------------
    // 8. 저장 로직
    // ------------------------------------------------------------
    private fun saveRecord() {

        // 검색으로 단일 음식 선택한 경우
        if (singleNutritionResult != null) {
            val n = singleNutritionResult!!

            val req = MealRecordRequest(
                meal_type = selectedMealType,
                foods = listOf(
                    FoodData(
                        food_name = n.name ?: "-",
                        amount = n.grams ?: 100f,
                        kcal = n.calories ?: 0f,
                        carb = n.carbs ?: 0f,
                        protein = n.protein ?: 0f,
                        fat = n.fat ?: 0f,
                        sugar = n.sugar ?: 0f
                    )
                )
            )

            sendMealRecord(req)
            return
        }

        // YOLO 다중 객체 → 체크된 음식만 저장
        val selectedItems = nutritionList.filter { it.selected }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "저장할 음식을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val foodsToSave = selectedItems.map { n ->
            FoodData(
                food_name = n.name ?: "-",
                amount = n.grams ?: 100f,
                kcal = n.calories ?: 0f,
                carb = n.carbs ?: 0f,
                protein = n.protein ?: 0f,
                fat = n.fat ?: 0f,
                sugar = n.sugar ?: 0f
            )
        }

        val req = MealRecordRequest(
            meal_type = selectedMealType,
            foods = foodsToSave
        )

        sendMealRecord(req)
    }


    private fun saveMultipleFoods(items: List<NutritionResponse>) {
        val foodList = items.map { n ->
            FoodData(
                food_name = n.name ?: "-",
                amount = n.grams ?: 1f,
                kcal = n.calories ?: 0f,
                carb = n.carbs ?: 0f,
                protein = n.protein ?: 0f,
                fat = n.fat ?: 0f,
                sugar = n.sugar ?: 0f
            )
        }

        val req = MealRecordRequest(
            meal_type = selectedMealType,
            foods = foodList
        )

        sendMealRecord(req)
    }

    private fun sendMealRecord(req: MealRecordRequest) {
        RetrofitClient.api.createRecord(req)
            .enqueue(object : Callback<MealRecordResponse> {

                override fun onResponse(
                    call: Call<MealRecordResponse>,
                    response: Response<MealRecordResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MealRecordActivity, "저장 완료!", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@MealRecordActivity,
                            "저장 실패(서버 오류)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MealRecordResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MealRecordActivity,
                        "서버 연결 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // ------------------------------------------------------------
    // g 자동 비율 계산
    // ------------------------------------------------------------
    private fun scaleNutrition(original: NutritionResponse, newGram: Int): NutritionResponse {

        val baseGram = original.grams ?: 100f
        val ratio = newGram.toFloat() / baseGram

        return NutritionResponse(
            name = original.name,
            calories = (original.calories ?: 0f) * ratio,
            grams = newGram.toFloat(),
            carbs = (original.carbs ?: 0f) * ratio,
            protein = (original.protein ?: 0f) * ratio,
            fat = (original.fat ?: 0f) * ratio,
            sugar = (original.sugar ?: 0f) * ratio,
            selected = original.selected
        )
    }

    // ------------------------------------------------------------
    // Bottom Navigation
    // ------------------------------------------------------------
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_record

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> startActivity(Intent(this, MainActivity::class.java))
                R.id.menu_analysis -> startActivity(Intent(this, AnalysisActivity::class.java))
                R.id.menu_mypage -> startActivity(Intent(this, MyPageActivity::class.java))
            }
            true
        }
    }

    // ------------------------------------------------------------
    // 카메라 기능
    // ------------------------------------------------------------
    private fun openCamera() {

        // 1) CAMERA 권한 체크
        if (checkSelfPermission(android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 2000)
            return
        }

        // 2) 사진 파일 생성
        val photoFile = createImageFile()

        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )

        // 3) 카메라 앱 호출 인텐트
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // FileProvider URI 접근 권한 부여
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        // 사진 저장 위치 전달
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)

        // 4) 인텐트 실행
        startActivityForResult(intent, CAMERA_REQUEST)
    }



    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val file = File.createTempFile(
            "IMG_${timeStamp}_",
            ".jpg",
            storageDir
        )

        currentPhotoPath = file.absolutePath
        return file
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 2000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()   // 권한 허용 → 다시 촬영 시작
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
