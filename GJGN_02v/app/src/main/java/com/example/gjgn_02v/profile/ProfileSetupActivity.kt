package com.example.gjgn_02v.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.model.UserProfileRequest
import com.example.gjgn_02v.model.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var inputName: EditText
    private lateinit var inputAge: EditText
    private lateinit var inputHeight: EditText
    private lateinit var inputWeight: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        inputName = findViewById(R.id.input_name)
        inputAge = findViewById(R.id.input_age)
        inputHeight = findViewById(R.id.input_height)
        inputWeight = findViewById(R.id.input_weight)
        btnSave = findViewById(R.id.btn_save_profile)

        loadProfile()
        saveProfileListener()
    }

    private fun loadProfile() {
        RetrofitClient.api.getMyProfile()
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (!response.isSuccessful) return
                    val p = response.body()!!

                    inputName.setText(p.name)
                    inputAge.setText(p.age.toString())
                    inputHeight.setText(p.height.toString())
                    inputWeight.setText(p.weight.toString())
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {}
            })
    }

    private fun saveProfileListener() {
        btnSave.setOnClickListener {
            val req = UserProfileRequest(
                name = inputName.text.toString(),
                age = inputAge.text.toString().toInt(),
                height = inputHeight.text.toString().toInt(),
                weight = inputWeight.text.toString().toInt()
            )

            RetrofitClient.api.createOrUpdateProfile(req)
                .enqueue(object : Callback<UserProfileResponse> {
                    override fun onResponse(
                        call: Call<UserProfileResponse>,
                        response: Response<UserProfileResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProfileSetupActivity, "프로필 저장 완료", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }

                    override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                        Toast.makeText(this@ProfileSetupActivity, "서버 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
