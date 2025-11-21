package com.example.gjgn_02v.data.model.mypage

import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.databinding.ActivityEditBirthBinding
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.UserProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditBirthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBirthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBirthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener { saveBirth() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun saveBirth() {

        val birth = extractBirthFromPicker(binding.datePicker)

        val request = UserProfileRequest(
            name = null,
            birth = birth,
            gender = null,
            height = null,
            weight = null,
            activity_level = null
        )

        RetrofitClient.api.updateMyProfile(request)
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    Toast.makeText(
                        this@EditBirthActivity,
                        "생년월일이 수정되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        this@EditBirthActivity,
                        "서버 오류: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun extractBirthFromPicker(picker: DatePicker): String {
        val year = picker.year
        val month = picker.month + 1  // 0-index → 1-index
        val day = picker.dayOfMonth

        return String.format("%04d-%02d-%02d", year, month, day)
    }
}
