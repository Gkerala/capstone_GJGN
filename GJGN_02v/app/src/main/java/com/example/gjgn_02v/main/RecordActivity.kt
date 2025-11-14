package com.example.gjgn_02v.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.ai.AiFoodDetectResponse
import com.example.gjgn_02v.data.model.foods.FoodItemResponse
import com.example.gjgn_02v.data.model.records.MealRecordRequest
import com.example.gjgn_02v.data.model.records.MealRecordResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RecordActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var listView: ListView
    private lateinit var saveButton: Button
    private lateinit var selectedFoodText: TextView
    private lateinit var btnSelectImage: Button
    private lateinit var spinnerMealType: Spinner

    private var foods = listOf<FoodItemResponse>()
    private var selectedFood: FoodItemResponse? = null
    private var selectedUri: Uri? = null

    private var selectedMealType = "breakfast"

    companion object {
        private const val PICK_IMAGE = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        searchInput = findViewById(R.id.search_input)
        listView = findViewById(R.id.list_foods)
        saveButton = findViewById(R.id.btn_save_meal)
        selectedFoodText = findViewById(R.id.txt_selected_food)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        spinnerMealType = findViewById(R.id.spinnerMealType)

        setupMealTypeSpinner()
        setupSearchListener()

        saveButton.setOnClickListener {
            if (selectedFood == null) {
                Toast.makeText(this, "음식을 선택하세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveSelectedFood()
        }

        btnSelectImage.setOnClickListener {
            pickImageFromGallery()
        }
    }


    // -------------------------------------------------------
    // 스피너
    // -------------------------------------------------------
    private fun setupMealTypeSpinner() {
        spinnerMealType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                selectedMealType = when (pos) {
                    0 -> "breakfast"
                    1 -> "lunch"
                    else -> "dinner"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // -------------------------------------------------------
    // 검색
    // -------------------------------------------------------
    private fun setupSearchListener() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim()
                if (q.length >= 2) searchFoods(q)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun searchFoods(query: String) {
        RetrofitClient.api.searchFoods(query)
            .enqueue(object : Callback<List<FoodItemResponse>> {
                override fun onResponse(call: Call<List<FoodItemResponse>>, res: Response<List<FoodItemResponse>>) {
                    if (res.isSuccessful && res.body() != null) {
                        foods = res.body()!!
                        val names = foods.map { it.name }

                        listView.adapter = ArrayAdapter(
                            this@RecordActivity,
                            android.R.layout.simple_list_item_1,
                            names
                        )

                        listView.setOnItemClickListener { _, _, i, _ ->
                            selectedFood = foods[i]
                            selectedFoodText.text = "선택됨: ${foods[i].name}"
                        }
                    }
                }
                override fun onFailure(call: Call<List<FoodItemResponse>>, t: Throwable) {}
            })
    }

    // -------------------------------------------------------
    // 수동 저장
    // -------------------------------------------------------
    private fun saveSelectedFood() {
        val food = selectedFood ?: return

        RetrofitClient.api.createRecord(
            MealRecordRequest(
                food_id = food.id,
                amount = 1,
                meal_type = selectedMealType
            )
        ).enqueue(object : Callback<MealRecordResponse> {

            override fun onResponse(call: Call<MealRecordResponse>, response: Response<MealRecordResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RecordActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<MealRecordResponse>, t: Throwable) {
                Toast.makeText(this@RecordActivity, "저장 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // -------------------------------------------------------
    // 이미지 선택
    // -------------------------------------------------------
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedUri = data?.data
            selectedUri?.let { uploadImage(it) }
        }
    }

    // -------------------------------------------------------
    // YOLO 업로드 + 자동 저장 흐름
    // -------------------------------------------------------
    private fun uploadImage(uri: Uri) {
        val file = File(getRealPathFromURI(uri))
        val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
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

                    if (result.items.isEmpty()) {
                        Toast.makeText(this@RecordActivity, "음식이 인식되지 않았습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // ⭐ 1) 최고 정확도 음식 자동 선택
                    val topItem = result.items.maxByOrNull { it.confidence }!!
                    val predictedLabel = topItem.label

                    // ⭐ 2) label로 DB 음식 검색 → 자동 선택 → 자동 저장
                    autoSearchAndSave(predictedLabel)
                }

                override fun onFailure(call: Call<AiFoodDetectResponse>, t: Throwable) {
                    Toast.makeText(this@RecordActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // -------------------------------------------------------
    // YOLO label → DB 검색 → 자동 저장
    // -------------------------------------------------------
    private fun autoSearchAndSave(label: String) {

        RetrofitClient.api.searchFoods(label)
            .enqueue(object : Callback<List<FoodItemResponse>> {
                override fun onResponse(call: Call<List<FoodItemResponse>>, response: Response<List<FoodItemResponse>>) {

                    if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                        AlertDialog.Builder(this@RecordActivity)
                            .setTitle("자동 저장 실패")
                            .setMessage("인식된 음식($label)과 일치하는 항목이 DB에 없습니다.")
                            .setPositiveButton("확인", null)
                            .show()
                        return
                    }

                    // ★ 가장 첫 번째 음식 자동 선택
                    val food = response.body()!![0]
                    selectedFood = food

                    // ★ 자동 저장 진행
                    RetrofitClient.api.createRecord(
                        MealRecordRequest(
                            food_id = food.id,
                            amount = 1,
                            meal_type = selectedMealType
                        )
                    ).enqueue(object : Callback<MealRecordResponse> {

                        override fun onResponse(call: Call<MealRecordResponse>, res: Response<MealRecordResponse>) {
                            AlertDialog.Builder(this@RecordActivity)
                                .setTitle("자동 저장 완료")
                                .setMessage("${food.name} 이(가) ${mealTypeKorean()} 식사로 자동 저장되었습니다!")
                                .setPositiveButton("확인") { _, _ -> finish() }
                                .show()
                        }

                        override fun onFailure(call: Call<MealRecordResponse>, t: Throwable) {
                            Toast.makeText(this@RecordActivity, "저장 실패", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                override fun onFailure(call: Call<List<FoodItemResponse>>, t: Throwable) {}
            })
    }

    // 아침 / 점심 / 저녁 한국어 변환
    private fun mealTypeKorean(): String {
        return when (selectedMealType) {
            "breakfast" -> "아침"
            "lunch" -> "점심"
            else -> "저녁"
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val idx = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val result = cursor?.getString(idx!!)
        cursor?.close()
        return result ?: ""
    }
}
