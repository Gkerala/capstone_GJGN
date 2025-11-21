package com.example.gjgn_02v.data.model.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.gjgn_02v.databinding.ActivityEditActivityLevelBinding
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.UpdateGoalRequest
import com.example.gjgn_02v.data.model.goals.UpdateGoalResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditActivityLevelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditActivityLevelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditActivityLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener { saveActivityLevel() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun saveActivityLevel() {
        val selectedId = binding.radioGroupActivity.checkedRadioButtonId

        if (selectedId == -1) {
            Toast.makeText(this, "활동량을 선택하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val activityLevel: Int = when (selectedId) {
            binding.rbVeryLow.id -> 1
            binding.rbLow.id -> 2
            binding.rbMedium.id -> 3
            binding.rbHigh.id -> 4
            binding.rbVeryHigh.id -> 5
            else -> 3
        }

        // Goal API 요청으로 활동량 수정
        val request = UpdateGoalRequest(
            activity_level = activityLevel
        )

        RetrofitClient.api.updateGoal(request)
            .enqueue(object : Callback<UpdateGoalResponse> {
                override fun onResponse(
                    call: Call<UpdateGoalResponse>,
                    response: Response<UpdateGoalResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EditActivityLevelActivity,
                            "활동량이 수정되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditActivityLevelActivity,
                            "수정 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UpdateGoalResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditActivityLevelActivity,
                        "서버 오류 발생",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
