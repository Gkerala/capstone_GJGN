package com.example.gjgn_02v.main

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GoalResetActivity : AppCompatActivity() {

    private lateinit var inputCal: EditText
    private lateinit var inputCarb: EditText
    private lateinit var inputProtein: EditText
    private lateinit var inputFat: EditText
    private lateinit var btnUpdate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_reset)

        inputCal = findViewById(R.id.goal_cal)
        inputCarb = findViewById(R.id.goal_carb)
        inputProtein = findViewById(R.id.goal_protein)
        inputFat = findViewById(R.id.goal_fat)
        btnUpdate = findViewById(R.id.btn_update_goal)

        loadCurrentGoal()
        btnUpdate.setOnClickListener { updateGoal() }
    }

    private fun loadCurrentGoal() {
        RetrofitClient.api.getGoal().enqueue(object : Callback<GoalResponse> {
            override fun onResponse(call: Call<GoalResponse>, res: Response<GoalResponse>) {
                if (res.isSuccessful) {
                    val g = res.body()!!
                    inputCal.setText(g.calorie.toString())
                    inputCarb.setText(g.carb.toString())
                    inputProtein.setText(g.protein.toString())
                    inputFat.setText(g.fat.toString())
                }
            }
            override fun onFailure(call: Call<GoalResponse>, t: Throwable) {}
        })
    }

    private fun updateGoal() {
        val req = GoalUpdateRequest(
            calorie = inputCal.text.toString().toInt(),
            carb = inputCarb.text.toString().toInt(),
            protein = inputProtein.text.toString().toInt(),
            fat = inputFat.text.toString().toInt()
        )

        RetrofitClient.api.updateGoal(req)
            .enqueue(object : Callback<GoalResponse> {
                override fun onResponse(call: Call<GoalResponse>, res: Response<GoalResponse>) {
                    if (res.isSuccessful) {
                        Toast.makeText(this@GoalResetActivity, "목표 수정 완료!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<GoalResponse>, t: Throwable) {
                    Toast.makeText(this@GoalResetActivity, "오류 발생", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
