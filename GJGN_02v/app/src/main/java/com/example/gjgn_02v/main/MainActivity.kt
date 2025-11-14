package com.example.gjgn_02v.main

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.utils.TokenManager
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.common.BaseResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        btnDelete = findViewById(R.id.btnDeleteAccount)

        btnDelete.setOnClickListener {
            deleteAccount()
        }
    }

    private fun deleteAccount() {
        RetrofitClient.api.deleteUser()
            .enqueue(object : Callback<BaseResponse> {
                override fun onResponse(
                    call: Call<BaseResponse>,
                    response: Response<BaseResponse>
                ) {
                    if (response.isSuccessful) {
                        TokenManager.clearTokens(this@MyPageActivity)
                        Toast.makeText(this@MyPageActivity,
                            "탈퇴 완료", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<BaseResponse>, t: Throwable) {}
            })
    }
}
