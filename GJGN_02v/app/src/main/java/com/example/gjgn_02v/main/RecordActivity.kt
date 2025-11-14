package com.example.gjgn_02v.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
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
    private var foods = listOf<FoodItemResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        searchInput = findViewById(R.id.search_input)
        listView = findViewById(R.id.list_foods)
        saveButton = findViewById(R.id.btn_save_meal)
        selectedFoodText = findViewById(R.id.txt_selected_food)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim()
                if (q.length >= 2) searchFoods(q)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        saveButton.setOnClickListener {
            if (selectedFood == null) {
                Toast.makeText(this, "음식을 선택하세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveSelectedFood()
        }
    }

    private fun searchFoods(query: String) {
        RetrofitClient.api.searchFood(query)
            .enqueue(object : Callback<List<FoodItemResponse>> {
                override fun onResponse(
                    call: Call<List<FoodItemResponse>>,
                    res: Response<List<FoodItemResponse>>
                ) {
                    if (res.isSuccessful && res.body() != null) {
                        foods = res.body()!!
                        val names = foods.map { it.name }
                        listView.adapter = ArrayAdapter(this@RecordActivity, android.R.layout.simple_list_item_1, names)

                        listView.setOnItemClickListener { _, _, i, _ ->
                            selectedFood = foods[i]
                            selectedFoodText.text = "선택됨: ${foods[i].name}"
                        }
                    }
                }

                override fun onFailure(call: Call<List<FoodItemResponse>>, t: Throwable) {}
            })
    }

    private fun saveSelectedFood() {
        val food = selectedFood ?: return

        RetrofitClient.api.saveMeal(SaveMealRequest(food.id))
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, res: Response<BasicResponse>) {
                    if (res.isSuccessful) {
                        Toast.makeText(this@RecordActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(this@RecordActivity, "저장 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
