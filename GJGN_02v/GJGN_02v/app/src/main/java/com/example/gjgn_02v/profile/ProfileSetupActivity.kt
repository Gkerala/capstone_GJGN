package com.example.gjgn_02v.profile

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.UserProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var toggleGender: com.google.android.material.button.MaterialButtonToggleGroup
    private lateinit var btnMale: com.google.android.material.button.MaterialButton
    private lateinit var btnFemale: com.google.android.material.button.MaterialButton

    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var etAge: EditText
    private lateinit var btnSave: com.google.android.material.button.MaterialButton

    private var gender: String = "male"   // 기본값

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        // ───────────────────────────────
        // XML 매핑
        // ───────────────────────────────
        toggleGender = findViewById(R.id.toggleGender)
        btnMale = findViewById(R.id.btnMale)
        btnFemale = findViewById(R.id.btnFemale)

        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        etAge = findViewById(R.id.etAge)

        btnSave = findViewById(R.id.btnSaveProfile)

        // 성별 선택
        toggleGender.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                gender = if (checkedId == R.id.btnMale) "male" else "female"
            }
        }

        loadProfile()
        saveProfile()
    }

    private fun loadProfile() {
        RetrofitClient.api.getMyProfile()
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (!response.isSuccessful || response.body() == null) return
                    val p = response.body()!!

                    // Height & Weight
                    etHeight.setText(p.height.toInt().toString())
                    etWeight.setText(p.weight.toInt().toString())
                    etAge.setText(p.age.toString())

                    // Gender 선택
                    if (p.gender == "male") btnMale.isChecked = true
                    else btnFemale.isChecked = true
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {}
            })
    }

    private fun saveProfile() {
        btnSave.setOnClickListener {

            if (etHeight.text.isBlank() || etWeight.text.isBlank() || etAge.text.isBlank()) {
                Toast.makeText(this, "모든 값을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val req = UserProfileRequest(
                name = "사용자",                // 기본값 (XML에 입력창 없음)
                birth = "2000-01-01",          // 기본값
                gender = gender,
                height = etHeight.text.toString().toInt(),
                weight = etWeight.text.toString().toInt(),
                activity_level = "medium"       // 기본값
            )

            RetrofitClient.api.updateMyProfile(req)
                .enqueue(object : Callback<UserProfileResponse> {
                    override fun onResponse(
                        call: Call<UserProfileResponse>,
                        response: Response<UserProfileResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@ProfileSetupActivity,
                                "프로필 저장 완료",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@ProfileSetupActivity,
                                "저장 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                        Toast.makeText(
                            this@ProfileSetupActivity,
                            "서버 오류",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}
