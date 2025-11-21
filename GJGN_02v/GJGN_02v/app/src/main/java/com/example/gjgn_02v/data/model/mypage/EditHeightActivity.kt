package com.example.gjgn_02v.data.model.mypage

import android.os.Bundle
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.UserProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import com.example.gjgn_02v.databinding.ActivityEditHeightBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditHeightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditHeightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditHeightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNumberPickers()

        binding.btnSave.setOnClickListener { saveHeight() }
        binding.btnBack.setOnClickListener { finish() }
    }

    /** NumberPicker 기본 설정 */
    private fun setupNumberPickers() {
        val np100: NumberPicker = binding.npHeight100
        val np10: NumberPicker = binding.npHeight10
        val np1: NumberPicker = binding.npHeight1
        val npDecimal: NumberPicker = binding.npHeightDecimal

        // 100 단위 (100~200)
        np100.minValue = 1
        np100.maxValue = 2
        np100.value = 1

        // 10 단위
        np10.minValue = 0
        np10.maxValue = 9
        np10.value = 7

        // 1 단위
        np1.minValue = 0
        np1.maxValue = 9
        np1.value = 0

        // 소수점 1자리
        npDecimal.minValue = 0
        npDecimal.maxValue = 9
        npDecimal.value = 0
    }

    /** 서버에 키 저장 */
    private fun saveHeight() {
        val h100 = binding.npHeight100.value
        val h10 = binding.npHeight10.value
        val h1 = binding.npHeight1.value
        val dec = binding.npHeightDecimal.value / 10f

        val heightValue = (h100 * 100) + (h10 * 10) + h1 + dec

        val request = UserProfileRequest(
            name = null,
            birth = null,
            gender = null,
            height = heightValue,
            weight = null
        )

        RetrofitClient.api.updateMyProfile(request)
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EditHeightActivity,
                            "키가 성공적으로 수정되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditHeightActivity,
                            "수정 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditHeightActivity,
                        "서버 오류 발생",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
