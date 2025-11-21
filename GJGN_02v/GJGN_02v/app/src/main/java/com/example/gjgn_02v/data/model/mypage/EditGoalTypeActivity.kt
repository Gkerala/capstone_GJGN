package com.example.gjgn_02v.data.model.mypage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.databinding.ActivityEditGoalTypeBinding
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.UserGoalRequest
import com.example.gjgn_02v.data.model.auth.UserGoalResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditGoalTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditGoalTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditGoalTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener { saveGoalType() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun saveGoalType() {
        val selectedId = binding.radioGroupGoalType.checkedRadioButtonId

        if (selectedId == -1) {
            Toast.makeText(this, "목표 유형을 선택하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 서버가 사용하는 goal_type 값을 매핑
        val goalType: Int = when (selectedId) {
            binding.rbMaintain.id -> 1  // 유지
            binding.rbLose.id -> 2      // 감량
            binding.rbGain.id -> 3      // 증량
            else -> 1
        }

        val request = UserGoalRequest(
            goal_type = goalType,
            goal_weight = null
        )

        RetrofitClient.api.updateUserGoal(request)
            .enqueue(object : Callback<UserGoalResponse> {
                override fun onResponse(
                    call: Call<UserGoalResponse>,
                    response: Response<UserGoalResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EditGoalTypeActivity,
                            "목표 유형이 변경되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditGoalTypeActivity,
                            "수정 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UserGoalResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditGoalTypeActivity,
                        "서버 오류",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
