package com.example.gjgn_02v.data.model.mypage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.databinding.ActivityEditNameBinding
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.UserProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditNameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadMyProfile()

        binding.btnSave.setOnClickListener { updateName() }
        binding.btnBack.setOnClickListener { finish() }
    }

    /** 🔹 서버에서 현재 사용자 정보 불러오기 */
    private fun loadMyProfile() {
        RetrofitClient.api.getMyProfile()
            .enqueue(object : Callback<UserProfileResponse> {

                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        val profile = response.body()!!

                        // 🔥 현재 이름 표시
                        binding.tvCurrentName.text = "현재 이름: ${profile.name}"
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(this@EditNameActivity, "프로필 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /** 🔹 사용자 이름 업데이트 */
    private fun updateName() {
        val newName = binding.etName.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "새 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UserProfileRequest(name = newName)

        RetrofitClient.api.updateMyProfile(request)
            .enqueue(object : Callback<UserProfileResponse> {

                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditNameActivity, "이름이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditNameActivity, "저장 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(this@EditNameActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
