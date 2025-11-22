package com.example.gjgn_02v.data.model.mypage

import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.databinding.ActivityEditBirthBinding
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.FullProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditBirthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBirthBinding

    private var currentGender: String? = null
    private var currentHeight: Float? = null
    private var currentWeight: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBirthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCurrentProfile()

        binding.btnSave.setOnClickListener { saveBirth() }
        binding.btnBack.setOnClickListener { finish() }
    }

    /** 현재 사용자 정보 불러오기 */
    private fun loadCurrentProfile() {
        RetrofitClient.api.getMyProfile()
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    response.body()?.let { user ->
                        currentGender = user.gender
                        currentHeight = user.height
                        currentWeight = user.weight
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {}
            })
    }

    /** 생년월일 저장 */
    private fun saveBirth() {

        if (currentGender == null || currentHeight == null || currentWeight == null) {
            Toast.makeText(this, "사용자 정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val birth = extractBirthFromPicker(binding.datePicker)

        val request = FullProfileRequest(
            gender = currentGender!!,
            birth = birth,
            height = currentHeight!!,
            weight = currentWeight!!
        )

        RetrofitClient.api.updateFullProfile(request)
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

    /** DatePicker → YYYY-MM-DD 변환 */
    private fun extractBirthFromPicker(picker: DatePicker): String {
        val year = picker.year
        val month = picker.month + 1
        val day = picker.dayOfMonth

        return String.format("%04d-%02d-%02d", year, month, day)
    }
}
