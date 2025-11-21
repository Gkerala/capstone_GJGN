package com.example.gjgn_02v.data.model.mypage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.UserProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import com.example.gjgn_02v.databinding.ActivityEditNameBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditNameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 저장 버튼
        binding.btnSave.setOnClickListener { saveName() }

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun saveName() {
        val newName = binding.etName.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 서버 요청 형식 (회원가입/프로필 수정과 동일)
        val request = UserProfileRequest(name = newName)

        RetrofitClient.api.updateMyProfile(request)
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    Toast.makeText(this@EditNameActivity, "이름이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(this@EditNameActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })

    }
}
