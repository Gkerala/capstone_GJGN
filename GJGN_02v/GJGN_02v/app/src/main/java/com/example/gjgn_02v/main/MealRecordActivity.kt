package com.example.gjgn_02v.main

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.gjgn_02v.data.model.foods.NutritionResponse
import com.example.gjgn_02v.data.model.records.MealRecordRequest
import com.example.gjgn_02v.data.model.records.MealRecordResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import com.example.gjgn_02v.data.model.foods.FoodSearchResponse



class MealRecordActivity : AppCompatActivity() {

    private lateinit var btnBreakfast: Button
    private lateinit var btnLunch: Button
    private lateinit var btnDinner: Button

    private lateinit var btnTakePhoto: Button
    private lateinit var btnSelectImage: Button

    private lateinit var searchInput: EditText
    private lateinit var listSearch: ListView

    private lateinit var imgPreview: ImageView
    private lateinit var analysisContainer: LinearLayout

    private lateinit var btnSave: Button

    private var selectedFood: FoodItemResponse? = null
    private var singleNutritionResult: NutritionResponse? = null

    private val nutritionList = mutableListOf<NutritionResponse>()

    private var selectedMealType = "breakfast"
    private var foods = listOf<FoodItemResponse>()
    private var selectedUri: Uri? = null

    companion object {
        private const val PICK_IMAGE = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_record)

        initViews()
        setupMealButtons()
        setupSearchInput()
        setupBottomNav()

        btnTakePhoto.setOnClickListener {
            Toast.makeText(this, "카메라 기능은 추후 추가됩니다.", Toast.LENGTH_SHORT).show()
        }

        btnSelectImage.setOnClickListener { pickImageFromGallery() }
        btnSave.setOnClickListener { saveRecord() }
    }

    private fun initViews() {
        btnBreakfast = findViewById(R.id.btnBreakfast)
        btnLunch = findViewById(R.id.btnLunch)
        btnDinner = findViewById(R.id.btnDinner)

        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnSelectImage = findViewById(R.id.btnSelectImage)

        searchInput = findViewById(R.id.inputSearch)
        val searchResultContainer = findViewById<LinearLayout>(R.id.searchResultContainer)

        imgPreview = findViewById(R.id.imgPreview)
        analysisContainer = findViewById(R.id.analysisContainer)

        btnSave = findViewById(R.id.btnSave)

        analysisContainer.visibility = View.VISIBLE
    }

    // ------------------------------------------
    // 1) Meal buttons
    // ------------------------------------------
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

        btnBreakfast.setOnClickListener {
            selectedMealType = "breakfast"
            focus(btnBreakfast)
        }
        btnLunch.setOnClickListener {
            selectedMealType = "lunch"
            focus(btnLunch)
        }
        btnDinner.setOnClickListener {
            selectedMealType = "dinner"
            focus(btnDinner)
        }

        focus(btnBreakfast)
    }

    // ------------------------------------------
// 2) 음식 검색 + 디버그 로그 추가
// ------------------------------------------
    private fun setupSearchInput() {
        searchInput.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim()
                val searchContainer = findViewById<LinearLayout>(R.id.searchResultContainer)

                if (q.length >= 2) {
                    searchFoods(q)
                } else {
                    searchContainer.visibility = View.GONE
                    searchContainer.removeAllViews()
                }
            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun searchFoods(query: String) {

        Log.d("SEARCH_DEBUG", "searchFoods() 시작, query = $query")

        RetrofitClient.api.searchFoods(query)
            .enqueue(object : Callback<FoodSearchResponse> {

                override fun onResponse(
                    call: Call<FoodSearchResponse>,
                    res: Response<FoodSearchResponse>
                ) {
                    Log.d("SEARCH_DEBUG", "API 응답 도착")

                    val searchContainer = findViewById<LinearLayout>(R.id.searchResultContainer)
                    searchContainer.removeAllViews()

                    if (!res.isSuccessful) {
                        Log.e("SEARCH_DEBUG", "❌ 실패: ${res.code()} / ${res.errorBody()?.string()}")
                        searchContainer.visibility = View.GONE
                        return
                    }

                    val body = res.body()

                    if (body == null || body.results.isEmpty()) {
                        Log.d("SEARCH_DEBUG", "검색 결과 없음")
                        searchContainer.visibility = View.GONE
                        return
                    }

                    foods = body.results
                    searchContainer.visibility = View.VISIBLE

                    Log.d("SEARCH_DEBUG", "검색 결과 개수: ${foods.size}")

                    for (item in foods) {
                        val tv = TextView(this@MealRecordActivity).apply {
                            text = item.name
                            textSize = 16f
                            setPadding(30, 25, 30, 25)
                            setBackgroundResource(android.R.color.white)

                            setOnClickListener {
                                Log.d("SEARCH_DEBUG", "항목 클릭됨: ${item.name}")

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

                override fun onFailure(call: Call<FoodSearchResponse>, t: Throwable) {
                    Log.e("SEARCH_DEBUG", "❌ 검색 API 실패: ${t.message}")
                }
            })
    }




    // ------------------------------------------
    // 3) 이미지 선택
    // ------------------------------------------
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedUri = data?.data ?: return

            selectedFood = null
            singleNutritionResult = null

            imgPreview.setImageURI(selectedUri)
            imgPreview.visibility = View.VISIBLE

            analysisContainer.removeAllViews()
            nutritionList.clear()
            analysisContainer.visibility = View.VISIBLE

            uploadImage(selectedUri!!)
        }
    }

    // ------------------------------------------
    // 4) YOLO detect
    // ------------------------------------------
    private fun uploadImage(uri: Uri) {
        Log.d("RECORD_DEBUG", "uploadImage() 호출됨, URI = $uri")

        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            Log.e("RECORD_DEBUG", "❌ inputStream null")
            return
        }

        val bytes = inputStream.readBytes()
        inputStream.close()

        val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val multipart = MultipartBody.Part.createFormData(
            "image",
            "upload.jpg",
            reqFile
        )

        RetrofitClient.api.detectFood(multipart)
            .enqueue(object : Callback<AiFoodDetectResponse> {

                override fun onResponse(
                    call: Call<AiFoodDetectResponse>,
                    res: Response<AiFoodDetectResponse>
                ) {
                    if (!res.isSuccessful) {
                        Log.e("RECORD_DEBUG", "YOLO 실패: ${res.errorBody()?.string()}")
                        return
                    }

                    val result = res.body()
                    if (result == null || result.foods.isEmpty()) {
                        Toast.makeText(this@MealRecordActivity, "음식을 감지하지 못했습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    fetchNutritionForFoods(result.foods.map { it.name })
                }

                override fun onFailure(call: Call<AiFoodDetectResponse>, t: Throwable) {
                    Log.e("RECORD_DEBUG", "YOLO onFailure(): ${t.message}")
                }
            })
    }

    // ------------------------------------------
    // 5) Nutrition 여러 개 조회
    // ------------------------------------------
    private fun fetchNutritionForFoods(names: List<String>) {
        lifecycleScope.launch {
            nutritionList.clear()
            analysisContainer.removeAllViews()
            analysisContainer.visibility = View.VISIBLE

            // 음식명이 null 또는 빈 문자열일 수 있으므로 필터링
            val validNames = names.filter { !it.isNullOrBlank() }

            for (name in validNames) {
                try {
                    val res = RetrofitClient.api.getNutrition(name)

                    if (res.isSuccessful && res.body()?.success == true) {
                        val item = res.body()!!
                        nutritionList.add(item)
                    } else {
                        Log.e("NUTRITION", "⚠️ Fail: $name → ${res.errorBody()?.string()}")
                    }

                } catch (e: Exception) {
                    Log.e("NUTRITION", "❌ Exception: $name → ${e.message}")
                }
            }

            updateNutritionUI()
        }
    }

    // ------------------------------------------
    // 6) 감지된 음식 UI
    // ------------------------------------------
    private fun updateNutritionUI() {
        analysisContainer.removeAllViews()
        analysisContainer.visibility = View.VISIBLE

        nutritionList.forEachIndexed { idx, item ->
            val view = layoutInflater.inflate(R.layout.item_food_analysis, analysisContainer, false)

            val chk = view.findViewById<CheckBox>(R.id.chkSelect)
            val txtGrams = view.findViewById<TextView>(R.id.txtGrams)
            val txtKcal = view.findViewById<TextView>(R.id.txtKcal)
            val txtCarbs = view.findViewById<TextView>(R.id.txtCarbs)
            val txtProtein = view.findViewById<TextView>(R.id.txtProtein)
            val txtFat = view.findViewById<TextView>(R.id.txtFat)
            val txtSugar = view.findViewById<TextView>(R.id.txtSugar)
            val btnEdit = view.findViewById<Button>(R.id.btnEdit)

            // ---------------------
            // 초기 UI 표시
            // ---------------------
            view.findViewById<TextView>(R.id.txtName).text = item.name ?: "-"
            txtGrams.text = "${item.serving_size ?: "100"} g"
            txtKcal.text = "${item.calories ?: 0f} kcal"
            txtCarbs.text = "탄수화물: ${item.carbs ?: 0f}"
            txtProtein.text = "단백질: ${item.protein ?: 0f}"
            txtFat.text = "지방: ${item.fat ?: 0f}"
            txtSugar.text = "당: ${item.sugar ?: 0f}"

            // ---------------------
            // g 수정 → 자동 보정
            // ---------------------
            btnEdit.setOnClickListener {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("섭취량(g) 수정")

                val input = EditText(this)
                input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                input.hint = "예: 250"
                dialog.setView(input)

                dialog.setPositiveButton("변경") { _: DialogInterface, _: Int ->
                    val newGram = input.text.toString().toIntOrNull()

                    if (newGram != null && newGram > 0) {

                        // 자동 보정
                        val updated = scaleNutrition(item, newGram)

                        nutritionList[idx] = updated

                        txtGrams.text = "${updated.serving_size} g"
                        txtKcal.text = "${updated.calories} kcal"
                        txtCarbs.text = "탄수화물: ${updated.carbs}"
                        txtProtein.text = "단백질: ${updated.protein}"
                        txtFat.text = "지방: ${updated.fat}"
                        txtSugar.text = "당: ${updated.sugar}"
                    }
                }

                dialog.setNegativeButton("취소", null)
                dialog.show()
            }


            chk.setOnCheckedChangeListener { _, isChecked ->
                nutritionList[idx] = nutritionList[idx].copy(
                    serving_size = if (isChecked) "selected" else "unselected"
                )
            }

            analysisContainer.addView(view)
        }
    }





    // ------------------------------------------
    // 7) 단일 음식 Nutrition
    // ------------------------------------------
    private fun loadSingleNutrition(foodName: String) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getNutrition(foodName)

                if (!res.isSuccessful || res.body() == null) {
                    Log.e("NUTRITION", "❌ 단일 음식 조회 실패: ${res.errorBody()?.string()}")
                    return@launch
                }

                val n = res.body()!!
                singleNutritionResult = n

                analysisContainer.removeAllViews()
                analysisContainer.visibility = View.VISIBLE

                val view = layoutInflater.inflate(R.layout.item_food_analysis, analysisContainer, false)

                view.findViewById<TextView>(R.id.txtName).text = n.name ?: "-"
                view.findViewById<TextView>(R.id.txtKcal).text = "${n.calories ?: 0} kcal"
                view.findViewById<TextView>(R.id.txtCarbs).text = "탄수화물: ${n.carbs ?: 0}"
                view.findViewById<TextView>(R.id.txtProtein).text = "단백질: ${n.protein ?: 0}"
                view.findViewById<TextView>(R.id.txtFat).text = "지방: ${n.fat ?: 0}"
                view.findViewById<TextView>(R.id.txtSugar).text = "당: ${n.sugar ?: 0}"

                // 🔥 단일 음식 g 표시
                view.findViewById<TextView>(R.id.txtGrams).text =
                    "${n.serving_size ?: "100"} g"

                analysisContainer.addView(view)

            } catch (e: Exception) {
                Log.e("NUTRITION", "❌ loadSingleNutrition() Exception: ${e.message}")
            }
        }
    }


    // ------------------------------------------
    // 8) 저장 버튼
    // ------------------------------------------
    private fun saveRecord() {

        if (selectedFood != null) {
            val req = MealRecordRequest(
                food_id = selectedFood!!.id,
                meal_type = selectedMealType,
                amount = 1,
                name = selectedFood!!.name,
                calories = selectedFood!!.calorie,
                carbs = selectedFood!!.carb,
                protein = selectedFood!!.protein,
                fat = selectedFood!!.fat,
                sugar = null
            )
            sendRecordToServer(req)
            return
        }

        val selectedItems = nutritionList.filter { it.serving_size == "selected" }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "음식을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        saveMultipleFoods(selectedItems)
    }

    private fun sendRecordToServer(req: MealRecordRequest) {
        RetrofitClient.api.createRecord(req)
            .enqueue(object : Callback<MealRecordResponse> {
                override fun onResponse(
                    call: Call<MealRecordResponse>,
                    res: Response<MealRecordResponse>
                ) {
                    if (res.isSuccessful) {
                        Toast.makeText(this@MealRecordActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@MealRecordActivity, "저장 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MealRecordResponse>, t: Throwable) {
                    Toast.makeText(this@MealRecordActivity, "서버 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveMultipleFoods(items: List<NutritionResponse>) {
        lifecycleScope.launch {
            for (n in items) {
                val req = MealRecordRequest(
                    food_id = null,
                    meal_type = selectedMealType,
                    amount = 1,
                    name = n.name,
                    calories = n.calories,
                    carbs = n.carbs,
                    protein = n.protein,
                    fat = n.fat,
                    sugar = n.sugar
                )
                sendRecordToServer(req)
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
        cursor?.moveToFirst()
        val index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val result = cursor?.getString(index!!)
        cursor?.close()
        return result ?: ""
    }

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

    private fun scaleNutrition(
        original: NutritionResponse,
        newGram: Int
    ): NutritionResponse {

        // 원래 serving_size가 null 또는 숫자 아님 → 100g 기준
        val baseGram = (original.serving_size?.toIntOrNull() ?: 100)

        val ratio = newGram.toFloat() / baseGram.toFloat()

        return NutritionResponse(
            success = original.success,
            name = original.name,
            serving_size = newGram.toString(),
            calories = (original.calories ?: 0f) * ratio,
            carbs = (original.carbs ?: 0f) * ratio,
            protein = (original.protein ?: 0f) * ratio,
            fat = (original.fat ?: 0f) * ratio,
            sugar = (original.sugar ?: 0f) * ratio
        )
    }


}
