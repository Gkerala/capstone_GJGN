package com.example.gjgn_02v.profile

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.UserProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import com.example.gjgn_02v.main.MyPageActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var btnBirth: Button
    private lateinit var btnGender: Button
    private lateinit var btnActivity: Button
    private lateinit var btnHeight: Button
    private lateinit var btnWeight: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var birthDate: String = ""
    private var gender: String = "male"   // API는 male/female
    private var activityLevel: String = "medium" // API는 low/medium/high
    private var height: Int = 160
    private var weight: Int = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        // XML 연결
        etName = findViewById(R.id.etName)
        btnBirth = findViewById(R.id.btnBirth)
        btnGender = findViewById(R.id.btnGender)
        btnActivity = findViewById(R.id.btnActivityLevel)
        btnHeight = findViewById(R.id.btnSetHeight)
        btnWeight = findViewById(R.id.btnSetWeight)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        loadProfile()

        // -----------------------------
        // 생년월일 선택
        // -----------------------------
        btnBirth.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    birthDate = "%04d-%02d-%02d".format(y, m + 1, d)
                    btnBirth.text = "생년월일: $birthDate"
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // -----------------------------
        // 성별 선택
        // -----------------------------
        btnGender.setOnClickListener {
            val items = arrayOf("남성", "여성")
            val values = arrayOf("male", "female")

            AlertDialog.Builder(this)
                .setTitle("성별 선택")
                .setItems(items) { _, i ->
                    gender = values[i]
                    btnGender.text = "성별: ${items[i]}"
                }.show()
        }

        // -----------------------------
        // 활동량 선택 (low / medium / high)
        // -----------------------------
        btnActivity.setOnClickListener {
            val items = arrayOf("비활동적", "보통", "매우 활동적")
            val values = arrayOf("low", "medium", "high")

            AlertDialog.Builder(this)
                .setTitle("활동량 선택")
                .setItems(items) { _, i ->
                    activityLevel = values[i]
                    btnActivity.text = "활동량: ${items[i]}"
                }.show()
        }

        // -----------------------------
        // 키 설정
        // -----------------------------
        btnHeight.setOnClickListener {
            showNumberPicker("키(cm)", 100, 230) {
                height = it
                btnHeight.text = "키: $it cm"
            }
        }

        // -----------------------------
        // 몸무게 설정
        // -----------------------------
        btnWeight.setOnClickListener {
            showNumberPicker("몸무게(kg)", 30, 200) {
                weight = it
                btnWeight.text = "몸무게: $it kg"
            }
        }

        // -----------------------------
        // 저장
        // -----------------------------
        btnSave.setOnClickListener {

            val req = UserProfileRequest(
                name = etName.text.toString(),
                birth = birthDate,
                gender = gender,
                height = height,
                weight = weight,
                activity_level = activityLevel
            )

            RetrofitClient.api.updateMyProfile(req)
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

    // -----------------------------
    // 프로필 로드
    // -----------------------------
    private fun loadProfile() {
        RetrofitClient.api.getMyProfile()
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (!response.isSuccessful) return
                    val p = response.body()!!

                    etName.setText(p.nickname)

                    birthDate = p.created_at.substring(0, 10) // 서버에서 birth가 없음 → created_at 사용?
                    btnBirth.text = "생년월일: $birthDate"

                    gender = p.gender
                    btnGender.text = if (gender == "male") "성별: 남성" else "성별: 여성"

                    height = p.height.toInt()
                    weight = p.weight.toInt()
                    btnHeight.text = "키: $height cm"
                    btnWeight.text = "몸무게: $weight kg"

                    activityLevel = p.activity_level
                    btnActivity.text = when (activityLevel) {
                        "high" -> "활동량: 매우 활동적"
                        "medium" -> "활동량: 보통"
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
