package com.example.gjgn_02v.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecordActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var listView: ListView
    private lateinit var saveButton: Button
    private lateinit var selectedFoodText: TextView

    private var selectedFood: FoodItemResponse? = null
    private var foods: List<FoodItemResponse> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        searchInput = findViewById(R.id.search_input)
        listView = findViewById(R.id.list_foods)
        saveButton = findViewById(R.id.btn_save_meal)
        selectedFoodText = findViewById(R.id.txt_selected_food)

        searchFoodListener()
        saveMealListener()
    }

    // -----------------------
    // 음식 검색
    // -----------------------
    private fun searchFoodListener() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length < 2) return
                searchFoods(query)
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
                    response: Response<List<FoodItemResponse>>
                ) {
                    if (response.isSuccessful) {
                        foods = response.body() ?: listOf()
                        showFoodList()
                    }
                }

                override fun onFailure(call: Call<List<FoodItemResponse>>, t: Throwable) {
                    Toast.makeText(this@RecordActivity, "검색 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showFoodList() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            foods.map { it.name }
        )
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            selectedFood = foods[position]
            selectedFoodText.text = "선택된 음식: ${selectedFood!!.name}"
        }
    }

    // -----------------------
    // 식단 저장
    // -----------------------
    private fun saveMealListener() {
        saveButton.setOnClickListener {
            if (selectedFood == null) {
                Toast.makeText(this, "음식을 선택하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = MealRecordRequest(
                food_id = selectedFood!!.id,
                amount = 1  // 기본 1인분으로 저장
            )

            RetrofitClient.api.createRecord(request)
                .enqueue(object : Callback<MealRecordResponse> {
                    override fun onResponse(
                        call: Call<MealRecordResponse>,
                        response: Response<MealRecordResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@RecordActivity,
                                "식단 저장 완료!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }

                    override fun onFailure(call: Call<MealRecordResponse>, t: Throwable) {
                        Toast.makeText(
                            this@RecordActivity,
                            "네트워크 오류",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}