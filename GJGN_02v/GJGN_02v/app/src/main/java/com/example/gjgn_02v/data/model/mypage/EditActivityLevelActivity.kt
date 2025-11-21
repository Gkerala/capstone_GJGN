package com.example.gjgn_02v.data.model.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.gjgn_02v.databinding.ActivityEditActivityLevelBinding
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.UserProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
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

        val request = UserProfileRequest(
            name = null,
            birth = null,
            gender = null,
            height = null,
            weight = null,
            activity_level = activityLevel
        )

        RetrofitClient.api.updateMyProfile(request)
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
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

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditActivityLevelActivity,
                        "서버 오류 발생",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
