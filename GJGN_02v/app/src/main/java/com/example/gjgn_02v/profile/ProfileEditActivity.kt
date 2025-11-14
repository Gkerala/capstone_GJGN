package com.example.gjgn_02v.profile

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.utils.TokenManager
import com.example.gjgn_02v.utils.UserProfile
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.main.MyPageActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var etName: EditText
    private lateinit var tvBirth: TextView
    private lateinit var btnGender: Button
    private lateinit var btnActivityLevel: Button
    private lateinit var btnSetHeight: Button
    private lateinit var btnSetWeight: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var selectedGender: String? = null
    private var selectedActivity: String? = null
    private var height: Float? = null
    private var weight: Float? = null
    private var birthDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        tvTitle = findViewById(R.id.tvTitle)
        etName = findViewById(R.id.etName)
        tvBirth = findViewById(R.id.tvBirth)
        btnGender = findViewById(R.id.btnGender)
        btnActivityLevel = findViewById(R.id.btnActivityLevel)
        btnSetHeight = findViewById(R.id.btnSetHeight)
        btnSetWeight = findViewById(R.id.btnSetWeight)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        val token = TokenManager.getAccessToken(this)
        if (token != null) {
            loadUserProfile(token)
        }

        // 생년월일 선택
        tvBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, { _, y, m, d ->
                birthDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                tvBirth.text = "생년월일: $birthDate"
            }, year, month, day).show()
        }

        // 성별 선택
        btnGender.setOnClickListener {
            val items = arrayOf("남성", "여성")
            AlertDialog.Builder(this)
                .setTitle("성별 선택")
                .setItems(items) { _, which ->
                    selectedGender = if (which == 0) "M" else "F"
                    btnGender.text = "성별: ${items[which]}"
                }.show()
        }

        // 활동량 선택
        btnActivityLevel.setOnClickListener {
            val items = arrayOf("비활동적", "보통", "매우 활동적")
            val values = arrayOf("low", "moderate", "high")
            AlertDialog.Builder(this)
                .setTitle("활동량 선택")
                .setItems(items) { _, which ->
                    selectedActivity = values[which]
                    btnActivityLevel.text = "활동량: ${items[which]}"
                }.show()
        }

        // 키 설정
        btnSetHeight.setOnClickListener {
            showNumberPickerDialog("키(cm)", 100, 250) { value ->
                height = value.toFloat()
                btnSetHeight.text = "키: $value cm"
            }
        }

        // 몸무게 설정
        btnSetWeight.setOnClickListener {
            showNumberPickerDialog("몸무게(kg)", 30, 200) { value ->
                weight = value.toFloat()
                btnSetWeight.text = "몸무게: $value kg"
            }
        }

        btnSave.setOnClickListener {
            val tokenHeader = "Bearer ${TokenManager.getAccessToken(this)}"
            val updatedProfile = UserProfile(
                username = etName.text.toString(),
                email = "", // 서버에서 무시
                birth_date = birthDate ?: "",
                gender = selectedGender ?: "M",
                height_cm = height ?: 0f,
                weight_kg = weight ?: 0f,
                goal_type = null,
                target_weight_kg = 0f,
                activity_level = selectedActivity ?: "moderate",
                profile_completed = true
            )

            RetrofitClient.api.submitProfile(tokenHeader, updatedProfile)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProfileEditActivity, "프로필 수정 완료", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ProfileEditActivity, MyPageActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@ProfileEditActivity, "수정 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@ProfileEditActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun showNumberPickerDialog(title: String, min: Int, max: Int, onSelected: (Int) -> Unit) {
        val picker = NumberPicker(this)
        picker.minValue = min
        picker.maxValue = max
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(picker)
            .setPositiveButton("확인") { _, _ -> onSelected(picker.value) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun loadUserProfile(token: String) {
        RetrofitClient.api.getCurrentUser("Bearer $token")
            .enqueue(object : Callback<UserProfile> {
                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        etName.setText(user?.username ?: "")
                        height = user?.height_cm
                        weight = user?.weight_kg
                        birthDate = user?.birth_date
                        selectedGender = user?.gender ?: "M"
                        selectedActivity = user?.activity_level ?: "moderate"

                        tvBirth.text = "생년월일: ${birthDate ?: "미입력"}"
                        btnGender.text = if (selectedGender == "M") "성별: 남성" else "성별: 여성"
                        btnActivityLevel.text = when (selectedActivity) {
                            "high" -> "활동량: 매우 활동적"
                            "moderate" -> "활동량: 보통"
                            else -> "활동량: 비활동적"
                        }
                        btnSetHeight.text = "키: ${height ?: 0f} cm"
                        btnSetWeight.text = "몸무게: ${weight ?: 0f} kg"
                    } else {
                        Toast.makeText(this@ProfileEditActivity, "정보 로드 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                    Toast.makeText(this@ProfileEditActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
