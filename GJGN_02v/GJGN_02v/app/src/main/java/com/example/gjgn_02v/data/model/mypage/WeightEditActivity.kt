package com.example.gjgn_02v.data.model.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.gjgn_02v.databinding.ActivityEditWeightBinding
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.UserProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditWeightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditWeightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditWeightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPickers()

        binding.btnSave.setOnClickListener { saveWeight() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupPickers() {
        binding.npWeight100.minValue = 0
        binding.npWeight100.maxValue = 2

        binding.npWeight10.minValue = 0
        binding.npWeight10.maxValue = 9

        binding.npWeight1.minValue = 0
        binding.npWeight1.maxValue = 9

        binding.npWeightDecimal.minValue = 0
        binding.npWeightDecimal.maxValue = 9
    }

    private fun saveWeight() {
        val weightStr =
            "${binding.npWeight100.value}${binding.npWeight10.value}${binding.npWeight1.value}.${binding.npWeightDecimal.value}"

        val weight = weightStr.toFloatOrNull()

        if (weight == null) {
            Toast.makeText(this, "몸무게를 올바르게 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ UserProfileRequest 구조에 맞게 필요한 필드만 업데이트
        val request = UserProfileRequest(
            name = null,
            birth = null,
            gender = null,
            height = null,
            weight = weight
        )

        RetrofitClient.api.updateMyProfile(request)
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EditWeightActivity,
                            "몸무게가 수정되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditWeightActivity,
                            "수정 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditWeightActivity,
                        "서버 오류 발생",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
