package com.example.gjgn_02v

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GoalResetActivity : AppCompatActivity() {

    private lateinit var tvCurrentGoal: TextView
    private lateinit var btnGoalType: Button
    private lateinit var btnTargetWeight: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var currentUser: UserProfile? = null
    private var jwtToken: String? = null

    private var selectedGoalType: String? = null
    private var selectedTargetWeight: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_reset)

        tvCurrentGoal = findViewById(R.id.tvCurrentGoal)
        btnGoalType = findViewById(R.id.btnGoalType)
        btnTargetWeight = findViewById(R.id.btnTargetWeight)
        btnSave = findViewById(R.id.btnSaveGoal)
        btnCancel = findViewById(R.id.btnCancelGoal)

        jwtToken = TokenManager.getAccessToken(this)
        currentUser = intent.getSerializableExtra("userProfile") as? UserProfile

        // ✅ 기존 목표 표시
        currentUser?.let {
            tvCurrentGoal.text = """
                현재 목표 유형: ${translateGoal(it.goal_type)}
                목표 체중: ${it.target_weight_kg ?: "-"} kg
            """.trimIndent()

            selectedGoalType = it.goal_type
            selectedTargetWeight = it.target_weight_kg
        }

        // ✅ 목표유형 선택
        btnGoalType.setOnClickListener {
            showGoalTypeDialog()
        }

        // ✅ 목표 체중 선택
        btnTargetWeight.setOnClickListener {
            showTargetWeightDialog()
        }

        // ✅ 저장 버튼
        btnSave.setOnClickListener {
            saveGoalToServer()
        }

        // ✅ 취소 버튼
        btnCancel.setOnClickListener {
            finish()
        }
    }

    // ✅ 목표 유형 선택 다이얼로그
    private fun showGoalTypeDialog() {
        val options = arrayOf("감량 (loss)", "유지 (maintain)", "증가 (gain)")
        val values = arrayOf("loss", "maintain", "gain")

        AlertDialog.Builder(this)
            .setTitle("목표 유형 선택")
            .setItems(options) { _, which ->
                selectedGoalType = values[which]
                Toast.makeText(this, "선택됨: ${options[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // ✅ 목표 체중 선택 다이얼로그
    private fun showTargetWeightDialog() {
        val picker = NumberPicker(this).apply {
            minValue = 30
            maxValue = 200
            value = (selectedTargetWeight ?: 60f).toInt()
        }

        AlertDialog.Builder(this)
            .setTitle("목표 체중 선택 (kg)")
            .setView(picker)
            .setPositiveButton("확인") { _, _ ->
                selectedTargetWeight = picker.value.toFloat()
                Toast.makeText(this, "목표 체중: ${picker.value}kg", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // ✅ 서버에 목표 변경사항 저장
    private fun saveGoalToServer() {
        val token = jwtToken ?: return
        val user = currentUser ?: return

        val updatedProfile = user.copy(
            goal_type = selectedGoalType,
            target_weight_kg = selectedTargetWeight
        )

        RetrofitClient.api.submitProfile("Bearer $token", updatedProfile)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@GoalResetActivity, "✅ 목표 재설정 완료", Toast.LENGTH_SHORT).show()

                        // ✅ 완료 후 마이페이지로 복귀
                        val intent = Intent(this@GoalResetActivity, MyPageActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@GoalResetActivity, "❌ 저장 실패 (${response.code()})", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@GoalResetActivity, "⚠️ 네트워크 오류: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun translateGoal(code: String?): String {
        return when (code) {
            "loss" -> "감량"
            "maintain" -> "유지"
            "gain" -> "증가"
            else -> "-"
        }
    }
}
