package com.example.gjgn_02v.main

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.UpdateGoalRequest
import com.example.gjgn_02v.data.model.goals.UpdateGoalResponse
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

        btnUpdate.setOnClickListener { updateGoal() }
    }

    private fun updateGoal() {

        val req = UpdateGoalRequest(
            kcal = inputCal.text.toString().toInt(),
            carb = inputCarb.text.toString().toInt(),
            protein = inputProtein.text.toString().toInt(),
            fat = inputFat.text.toString().toInt()
        )

        RetrofitClient.api.updateGoal(req)
            .enqueue(object : Callback<UpdateGoalResponse> {
                override fun onResponse(
                    call: Call<UpdateGoalResponse>,
                    response: Response<UpdateGoalResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@GoalResetActivity, "목표 수정 완료!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<UpdateGoalResponse>, t: Throwable) {
                    Toast.makeText(this@GoalResetActivity, "오류 발생", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
