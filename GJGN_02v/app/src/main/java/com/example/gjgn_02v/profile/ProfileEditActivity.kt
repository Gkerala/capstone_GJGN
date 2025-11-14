package com.example.gjgn_02v.profile

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.api.TokenStore
import com.example.gjgn_02v.main.MyPageActivity
import com.example.gjgn_02v.model.UserProfileRequest
import com.example.gjgn_02v.model.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var tvBirth: TextView
    private lateinit var btnGender: Button
    private lateinit var btnActivity: Button
    private lateinit var btnHeight: Button
    private lateinit var btnWeight: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var birthDate: String? = null
    private var gender: String = "M"
    private var activityLevel: String = "moderate"
    private var height: Int = 0
    private var weight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        etName = findViewById(R.id.etName)
        tvBirth = findViewById(R.id.tvBirth)
        btnGender = findViewById(R.id.btnGender)
        btnActivity = findViewById(R.id.btnActivityLevel)
        btnHeight = findViewById(R.id.btnSetHeight)
        btnWeight = findViewById(R.id.btnSetWeight)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        loadProfile()

        // --- 생년월일 ---
        tvBirth.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    birthDate = "%04d-%02d-%02d".format(y, m + 1, d)
                    tvBirth.text = "생년월일: $birthDate"
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // --- 성별 ---
        btnGender.setOnClickListener {
            val items = arrayOf("남성", "여성")
            AlertDialog.Builder(this)
                .setTitle("성별 선택")
                .setItems(items) { _, i ->
                    gender = if (i == 0) "M" else "F"
                    btnGender.text = "성별: ${items[i]}"
                }.show()
        }

        // --- 활동량 ---
        btnActivity.setOnClickListener {
            val items = arrayOf("비활동적", "보통", "매우 활동적")
            val values = arrayOf("low", "moderate", "high")

            AlertDialog.Builder(this)
                .setTitle("활동량 선택")
                .setItems(items) { _, i ->
                    activityLevel = values[i]
                    btnActivity.text = "활동량: ${items[i]}"
                }.show()
        }

        // --- 키 ---
        btnHeight.setOnClickListener {
            showNumberPicker("키(cm)", 100, 250) {
                height = it
                btnHeight.text = "키: $it cm"
            }
        }

        // --- 몸무게 ---
        btnWeight.setOnClickListener {
            showNumberPicker("몸무게(kg)", 30, 200) {
                weight = it
                btnWeight.text = "몸무게: $it kg"
            }
        }

        // --- 저장 ---
        btnSave.setOnClickListener {
            val req = UserProfileRequest(
                name = etName.text.toString(),
                age = 0, // Django에서 사용 X
                birth = birthDate ?: "",
                gender = gender,
                height = height,
                weight = weight,
                activityLevel = activityLevel
            )

            RetrofitClient.api.createOrUpdateProfile(req)
                .enqueue(object : Callback<UserProfileResponse> {
                    override fun onResponse(
                        call: Call<UserProfileResponse>,
                        response: Response<UserProfileResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProfileEditActivity, "프로필 수정 완료", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ProfileEditActivity, MyPageActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@ProfileEditActivity, "수정 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                        Toast.makeText(this@ProfileEditActivity, "서버 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        btnCancel.setOnClickListener { finish() }
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

                    etName.setText(p.name)
                    birthDate = p.birth
                    gender = p.gender
                    height = p.height
                    weight = p.weight
                    activityLevel = p.activityLevel

                    tvBirth.text = "생년월일: $birthDate"
                    btnGender.text = if (gender == "M") "성별: 남성" else "성별: 여성"
                    btnHeight.text = "키: $height cm"
                    btnWeight.text = "몸무게: $weight kg"

                    btnActivity.text = when (activityLevel) {
                        "high" -> "활동량: 매우 활동적"
                        "moderate" -> "활동량: 보통"
                        else -> "활동량: 비활동적"
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {}
            })
    }

    private fun showNumberPicker(title: String, min: Int, max: Int, callback: (Int) -> Unit) {
        val picker = NumberPicker(this)
        picker.minValue = min
        picker.maxValue = max

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(picker)
            .setPositiveButton("확인") { _, _ -> callback(picker.value) }
            .setNegativeButton("취소", null)
            .show()
    }
}
