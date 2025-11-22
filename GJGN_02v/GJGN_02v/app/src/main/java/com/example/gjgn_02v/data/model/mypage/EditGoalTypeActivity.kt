package com.example.gjgn_02v.data.model.mypage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import com.example.gjgn_02v.data.model.goals.UpdateGoalRequest
import com.example.gjgn_02v.data.model.goals.UpdateGoalResponse
import com.example.gjgn_02v.databinding.ActivityEditGoalTypeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditGoalTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditGoalTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditGoalTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCurrentGoalType()   // ⭐ 현재 목표 유형 불러오기

        binding.btnSave.setOnClickListener { saveGoalType() }
        binding.btnBack.setOnClickListener { finish() }
    }

    /** ⭐ 현재 목표 유형 서버에서 가져와 라디오 버튼 선택 */
    private fun loadCurrentGoalType() {
        RetrofitClient.api.getMyProfile()
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    val user = response.body() ?: return

                    when (user.goal_type) {
                        1 -> binding.rbLoseWeight.isChecked = true
                        2 -> binding.rbKeepWeight.isChecked = true
                        3 -> binding.rbGainWeight.isChecked = true
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditGoalTypeActivity,
                        "현재 목표 유형을 불러오지 못했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /** ⭐ 목표 유형 저장 */
    private fun saveGoalType() {
        val selectedId = binding.radioGroupGoalType.checkedRadioButtonId

        if (selectedId == -1) {
            Toast.makeText(this, "목표 유형을 선택하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val goalType: Int = when (selectedId) {
            binding.rbLoseWeight.id -> 1
            binding.rbKeepWeight.id -> 2
            binding.rbGainWeight.id -> 3
            else -> 2
        }

        val request = UpdateGoalRequest(
            goal_type = goalType
        )

        RetrofitClient.api.updateGoal(request)
            .enqueue(object : Callback<UpdateGoalResponse> {
                override fun onResponse(
                    call: Call<UpdateGoalResponse>,
                    response: Response<UpdateGoalResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EditGoalTypeActivity,
                            "목표 유형이 수정되었습니다.",
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

                override fun onFailure(call: Call<UpdateGoalResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditGoalTypeActivity,
                        "서버 오류 발생",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
