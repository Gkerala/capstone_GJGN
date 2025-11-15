package com.example.gjgn_02v.main

import android.app.Activity
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
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RecordActivity : AppCompatActivity() {

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

    // 검색해서 선택된 음식
    private var selectedFood: FoodItemResponse? = null

    // Nutrition API 단일 결과 저장
    private var singleNutritionResult: NutritionResponse? = null

    // YOLO 감지 음식 리스트
    private val nutritionList = mutableListOf<NutritionResponse>()

    private var selectedMealType = "breakfast"
    private var foods = listOf<FoodItemResponse>()
    private var selectedUri: Uri? = null

    companion object {
        private const val PICK_IMAGE = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

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
        listSearch = findViewById(R.id.listSearchResults)

        imgPreview = findViewById(R.id.imgPreview)
        analysisContainer = findViewById(R.id.analysisContainer)

        btnSave = findViewById(R.id.btnSave)
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
    // 2) 음식 검색
    // ------------------------------------------
    private fun setupSearchInput() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim()
                if (q.length >= 2) searchFoods(q)
                else listSearch.visibility = View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun searchFoods(query: String) {
        RetrofitClient.api.searchFoods(query)
            .enqueue(object : Callback<List<FoodItemResponse>> {
                override fun onResponse(
                    call: Call<List<FoodItemResponse>>,
                    res: Response<List<FoodItemResponse>>
                ) {
                    if (!res.isSuccessful || res.body().isNullOrEmpty()) {
                        listSearch.visibility = View.GONE
                        return
                    }

                    foods = res.body()!!
                    val names = foods.map { it.name }

                    listSearch.adapter = ArrayAdapter(
                        this@RecordActivity,
                        android.R.layout.simple_list_item_1,
                        names
                    )
                    listSearch.visibility = View.VISIBLE

                    listSearch.setOnItemClickListener { _, _, i, _ ->
                        selectedFood = foods[i]
                        selectedUri = null
                        imgPreview.visibility = View.GONE

                        loadSingleNutrition(foods[i].name)
                        listSearch.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<List<FoodItemResponse>>, t: Throwable) {}
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

            // 검색 음식 초기화
            selectedFood = null
            singleNutritionResult = null

            imgPreview.setImageURI(selectedUri)
            imgPreview.visibility = View.VISIBLE

            analysisContainer.removeAllViews()
            nutritionList.clear()

            uploadImage(selectedUri!!)
        }
    }

    // ------------------------------------------
    // 4) YOLO 음식 감지
    // ------------------------------------------
    private fun uploadImage(uri: Uri) {
        Log.d("RECORD_DEBUG", "uploadImage() 호출됨, URI = $uri")
        val file = File(getRealPathFromURI(uri))
        val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        Log.d("RECORD_DEBUG", "파일 경로 = ${file.absolutePath}")
        val multipart = MultipartBody.Part.createFormData("image", file.name, reqFile)

        RetrofitClient.api.detectFood(multipart)
            .enqueue(object : Callback<AiFoodDetectResponse> {
                override fun onResponse(
                    call: Call<AiFoodDetectResponse>,
                    res: Response<AiFoodDetectResponse>
                ) {
                    if (!res.isSuccessful || res.body() == null) {
                        Toast.makeText(this@RecordActivity, "서버 오류", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val result = res.body()!!
                    Log.d("RECORD_DEBUG", "YOLO 감지 결과 = ${result.foods}")
                    if (result.foods.isEmpty()) {
                        Toast.makeText(this@RecordActivity, "음식을 감지하지 못했습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val names = result.foods.map { it.name }
                    fetchNutritionForFoods(names)
                }

                override fun onFailure(call: Call<AiFoodDetectResponse>, t: Throwable) {}
            })
    }

    // ------------------------------------------
    // 5) Nutrition API 여러 개 조회
    // ------------------------------------------
    private fun fetchNutritionForFoods(names: List<String>) {
        lifecycleScope.launch {
            nutritionList.clear()

            for (name in names) {
                try {
                    val res = RetrofitClient.api.getNutrition(name)
                    if (res.isSuccessful && res.body()?.success == true) {
                        nutritionList.add(res.body()!!)
                        Log.d("RECORD_DEBUG", "Nutrition 조회 시작: ${names.size}개")
                    }
                } catch (_: Exception) {}
            }

            updateNutritionUI()
        }
    }

    // ------------------------------------------
    // 6) 감지된 음식 UI
    // ------------------------------------------
    private fun updateNutritionUI() {
        Log.d("RECORD_DEBUG", "updateNutritionUI() 호출됨, nutritionList size = ${nutritionList.size}")
        analysisContainer.removeAllViews()

        nutritionList.forEachIndexed { idx, item ->
            val view = layoutInflater.inflate(R.layout.item_food_analysis, analysisContainer, false)

            val chk = view.findViewById<CheckBox>(R.id.chkSelect)
            view.findViewById<TextView>(R.id.txtName).text = item.name
            view.findViewById<TextView>(R.id.txtKcal).text = "${item.calories ?: 0} kcal"
            view.findViewById<TextView>(R.id.txtCarbs).text = "탄수화물: ${item.carbs ?: 0}"
            view.findViewById<TextView>(R.id.txtProtein).text = "단백질: ${item.protein ?: 0}"
            view.findViewById<TextView>(R.id.txtFat).text = "지방: ${item.fat ?: 0}"
            view.findViewById<TextView>(R.id.txtSugar).text = "당: ${item.sugar ?: 0}"

            chk.setOnCheckedChangeListener { _, isChecked ->
                nutritionList[idx] = nutritionList[idx].copy(
                    serving_size = if (isChecked) "selected" else "unselected"
                )
            }

            analysisContainer.addView(view)
        }
    }

    // ------------------------------------------
    // 🔥 단일 음식 영양정보
    // ------------------------------------------
    private fun loadSingleNutrition(foodName: String) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getNutrition(foodName)
                Log.d("RECORD_DEBUG", "Nutrition 응답 (${foodName}): code=${res.code()}, body=${res.body()}")

                if (!res.isSuccessful || res.body() == null) {
                    Toast.makeText(this@RecordActivity, "영양정보 없음", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val n = res.body()!!
                singleNutritionResult = n

                analysisContainer.removeAllViews()

                val view = layoutInflater.inflate(R.layout.item_food_analysis, analysisContainer, false)

                view.findViewById<TextView>(R.id.txtName).text = n.name ?: "-"
                view.findViewById<TextView>(R.id.txtKcal).text = "${n.calories ?: 0} kcal"
                view.findViewById<TextView>(R.id.txtCarbs).text = "탄수화물: ${n.carbs ?: 0}"
                view.findViewById<TextView>(R.id.txtProtein).text = "단백질: ${n.protein ?: 0}"
                view.findViewById<TextView>(R.id.txtFat).text = "지방: ${n.fat ?: 0}"
                view.findViewById<TextView>(R.id.txtSugar).text = "당: ${n.sugar ?: 0}"

                // 단일이므로 체크박스 비활성
                view.findViewById<CheckBox>(R.id.chkSelect).visibility = View.GONE

                analysisContainer.addView(view)

            } catch (e: Exception) {
                Toast.makeText(this@RecordActivity, "영양정보 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ------------------------------------------
    // 🔥 저장 버튼 → 검색/YOLO 모두 처리
    // ------------------------------------------
    private fun saveRecord() {

        // ----------------------------
        // 1) 검색으로 선택된 경우
        // ----------------------------
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

        // ----------------------------
        // 2) YOLO 감지 → 체크된 음식만 저장
        // ----------------------------
        val selectedItems = nutritionList.filter { it.serving_size == "selected" }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "음식을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 여러 항목 저장
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
                        Toast.makeText(this@RecordActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@RecordActivity, "저장 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MealRecordResponse>, t: Throwable) {
                    Toast.makeText(this@RecordActivity, "서버 오류", Toast.LENGTH_SHORT).show()
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
}
