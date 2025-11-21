package com.example.gjgn_02v.data.model.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.gjgn_02v.databinding.ActivityEditGoalWeightBinding
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.UpdateGoalRequest
import com.example.gjgn_02v.data.model.goals.UpdateGoalResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditGoalWeightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditGoalWeightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditGoalWeightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPickers()

        binding.btnSave.setOnClickListener { saveGoalWeight() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupPickers() {
        binding.npGoalWeight100.minValue = 0
        binding.npGoalWeight100.maxValue = 2

        binding.npGoalWeight10.minValue = 0
        binding.npGoalWeight10.maxValue = 9

        binding.npGoalWeight1.minValue = 0
        binding.npGoalWeight1.maxValue = 9

        binding.npGoalWeightDecimal.minValue = 0
        binding.npGoalWeightDecimal.maxValue = 9
    }

    private fun saveGoalWeight() {
        val weightStr =
            "${binding.npGoalWeight100.value}${binding.npGoalWeight10.value}${binding.npGoalWeight1.value}.${binding.npGoalWeightDecimal.value}"

        val goalWeight = weightStr.toFloatOrNull()

        if (goalWeight == null) {
            Toast.makeText(this, "목표 체중을 올바르게 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UpdateGoalRequest(
            goal_weight = goalWeight
        )

        RetrofitClient.api.updateGoal(request)
            .enqueue(object : Callback<UpdateGoalResponse> {
                override fun onResponse(
                    call: Call<UpdateGoalResponse>,
                    response: Response<UpdateGoalResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EditGoalWeightActivity,
                            "목표 체중이 수정되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditGoalWeightActivity,
                            "수정 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UpdateGoalResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditGoalWeightActivity,
                        "서버 오류 발생",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
