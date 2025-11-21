package com.example.gjgn_02v.data.model.mypage

import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.UserProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import com.example.gjgn_02v.databinding.ActivityEditGenderBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditGenderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditGenderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditGenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 저장
        binding.btnSave.setOnClickListener { saveGender() }

        // 뒤로가기
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun saveGender() {

        val gender = when (binding.radioGender.checkedRadioButtonId) {
            binding.radioMale.id -> "male"
            binding.radioFemale.id -> "female"
            else -> null
        }

        if (gender == null) {
            Toast.makeText(this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UserProfileRequest(
            name = null,
            birth = null,
            gender = gender,
            height = null,
            weight = null
        )

        RetrofitClient.api.updateMyProfile(request)
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    Toast.makeText(
                        this@EditGenderActivity,
                        "성별이 수정되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditGenderActivity,
                        "서버 오류: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
