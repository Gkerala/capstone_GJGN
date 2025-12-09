package com.example.gjgn_02v.data.model.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.gjgn_02v.databinding.ActivityEditActivityLevelBinding
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.goals.UpdateGoalRequest
import com.example.gjgn_02v.data.model.goals.UpdateGoalResponse
import com.example.gjgn_02v.data.model.goals.UserGoalResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditActivityLevelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditActivityLevelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditActivityLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCurrentActivityLevel()

        binding.btnSave.setOnClickListener { saveActivityLevel() }
        binding.btnBack.setOnClickListener { finish() }
    }

    /** 🔹 현재 활동량(activity_level)을 API에서 불러와 라디오 버튼 체크 */
    private fun loadCurrentActivityLevel() {
        RetrofitClient.api.getGoal()
            .enqueue(object : Callback<UserGoalResponse> {
                override fun onResponse(
                    call: Call<UserGoalResponse>,
                    response: Response<UserGoalResponse>
                ) {
                    val goal = response.body() ?: return
                    setCheckedRadio(goal.activity_level)
                }

                override fun onFailure(call: Call<UserGoalResponse>, t: Throwable) {
                }
            })
    }

    /** 🔹 API에서 받아온 level값(1~5)을 라디오 버튼에 매핑 */
    private fun setCheckedRadio(level: Int) {
        when (level) {
            1 -> binding.rbVeryLow.isChecked = true
            2 -> binding.rbLow.isChecked = true
            3 -> binding.rbMedium.isChecked = true
            4 -> binding.rbHigh.isChecked = true
            5 -> binding.rbVeryHigh.isChecked = true
        }
    }

    /** 🔹 활동량 저장 */
    private fun saveActivityLevel() {
        val selectedId = binding.radioGroupActivity.checkedRadioButtonId

        if (selectedId == -1) {
            Toast.makeText(this, "활동량을 선택하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val activityLevel = when (selectedId) {
            binding.rbVeryLow.id -> 1
            binding.rbLow.id -> 2
            binding.rbMedium.id -> 3
            binding.rbHigh.id -> 4
            binding.rbVeryHigh.id -> 5
            else -> 3
        }

        val request = UpdateGoalRequest(activity_level = activityLevel)

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
                            "수정 실패 (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UpdateGoalResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditActivityLevelActivity,
                        "서버 오류 발생: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
