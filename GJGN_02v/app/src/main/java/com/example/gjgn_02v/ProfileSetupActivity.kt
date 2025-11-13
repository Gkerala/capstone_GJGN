package com.example.gjgn_02v

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ProfileSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val jwt = intent.getStringExtra("JWT_TOKEN") ?: ""
        val email = intent.getStringExtra("USER_EMAIL") ?: "kakao_user@kakao.com"

        setContent {
            ProfileSetupScreen(jwtToken = jwt, email = email)
        }
    }
}

@Composable
fun ProfileSetupScreen(jwtToken: String, email: String) {
    var step by remember { mutableStateOf(0) }

    var name by remember { mutableStateOf("") }
    var birth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var goalType by remember { mutableStateOf<String?>(null) }
    var targetWeight by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val authHeader = "Bearer $jwtToken"

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (step) {
                0 -> {
                    Text("이름을 입력하세요")
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                1 -> {
                    Text("생년월일 선택 (YYYY-MM-DD)")
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = {
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        DatePickerDialog(context, { _, y, m, d ->
                            birth = String.format("%04d-%02d-%02d", y, m + 1, d)
                        }, year, month, day).show()
                    }) {
                        Text(if (birth.isBlank()) "날짜 선택" else "선택됨: $birth")
                    }
                }

                2 -> {
                    Text("성별 선택")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenderOption("M", gender) { gender = "M" }
                        GenderOption("F", gender) { gender = "F" }
                    }
                }

                // ✅ 키·몸무게
                3 -> {
                    Text("키 선택 (1~999cm)")
                    TripleDialPicker("cm", height) { height = it }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("몸무게 선택 (1~999kg)")
                    TripleDialPicker("kg", weight) { weight = it }
                }

                // ✅ 목표 타입
                4 -> {
                    Text("목표 타입")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GoalOption("loss", goalType) { goalType = "loss" }
                        GoalOption("maintain", goalType) { goalType = "maintain" }
                        GoalOption("gain", goalType) { goalType = "gain" }
                    }
                }

                // ✅ 목표 체중
                5 -> {
                    Text("목표 체중 (1~999kg)")
                    TripleDialPicker("kg", targetWeight) { targetWeight = it }
                }

                // ✅ 활동량
                6 -> {
                    Text("활동량 선택")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ActivityOption("high", activityLevel) { activityLevel = "high" }
                        ActivityOption("moderate", activityLevel) { activityLevel = "moderate" }
                        ActivityOption("low", activityLevel) { activityLevel = "low" }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 0) Button(onClick = { step-- }) { Text("이전") }
                else Spacer(modifier = Modifier.width(1.dp))

                Button(
                    onClick = {
                        // ✅ step별 유효성 검사 추가
                        when (step) {
                            3 -> {
                                if (height.isBlank() || weight.isBlank() ||
                                    height.toFloatOrNull() == null || weight.toFloatOrNull() == null ||
                                    height.toFloat() <= 0 || weight.toFloat() <= 0
                                ) {
                                    Toast.makeText(context, "키와 몸무게를 입력해주세요.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                            }

                            5 -> {
                                if (targetWeight.isBlank() ||
                                    targetWeight.toFloatOrNull() == null ||
                                    targetWeight.toFloat() <= 0
                                ) {
                                    Toast.makeText(context, "목표 체중을 입력해주세요.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                            }
                        }

                        if (step < 6) {
                            step++
                        } else {
                            submitProfile(
                                context, authHeader, email, name, birth, gender,
                                height, weight, goalType, targetWeight, activityLevel
                            )
                        }
                    }
                ) {
                    Text(if (step < 6) "다음" else "제출")
                }
            }
        }
    }
}

private fun submitProfile(
    context: android.content.Context,
    authHeader: String,
    email: String,
    name: String,
    birth: String,
    gender: String?,
    height: String,
    weight: String,
    goalType: String?,
    targetWeight: String,
    activityLevel: String?
) {
    val profile = UserProfile(
        username = name,
        email = email,
        birth_date = birth,
        gender = gender ?: "M",
        height_cm = height.toFloatOrNull() ?: 0f,
        weight_kg = weight.toFloatOrNull() ?: 0f,
        goal_type = goalType ?: "maintain",
        target_weight_kg = targetWeight.toFloatOrNull() ?: 0f,
        activity_level = activityLevel ?: "moderate",
        profile_completed = true
    )

    RetrofitClient.api.submitProfile(authHeader, profile)
        .enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "✅ 프로필 저장 완료!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "❌ 저장 실패 (${response.code()})", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "⚠️ 네트워크 오류: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
}

/* -------------------------------
   ✅ 3자리 다이얼 (100·10·1)
-------------------------------- */
@Composable
fun TripleDialPicker(unit: String, value: String, onValueChange: (String) -> Unit) {
    val context = LocalContext.current
    Button(onClick = {
        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }
        val pickers = List(3) { NumberPicker(context).apply { minValue = 0; maxValue = 9 } }
        pickers.forEach { dialogView.addView(it) }

        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("값 선택 ($unit)")
        builder.setView(dialogView)
        builder.setPositiveButton("확인") { _, _ ->
            val num = pickers[0].value * 100 + pickers[1].value * 10 + pickers[2].value
            onValueChange(num.toString())
        }
        builder.setNegativeButton("취소", null)
        builder.show()
    }) {
        Text(if (value.isBlank()) "선택하기" else "$value $unit")
    }
}

/* -------------------------------
   ✅ 선택 버튼들
-------------------------------- */
@Composable
fun GenderOption(label: String, selected: String?, onClick: () -> Unit) {
    val isSelected = selected == label
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 24.dp)
    ) {
        Text(
            text = if (label == "M") "남성" else "여성",
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun GoalOption(label: String, selected: String?, onClick: () -> Unit) {
    val isSelected = selected == label
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF4CAF50) else Color(0xFFD6D6D6))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 24.dp)
    ) {
        Text(
            text = when (label) {
                "loss" -> "감량"
                "maintain" -> "유지"
                else -> "증가"
            },
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ActivityOption(label: String, selected: String?, onClick: () -> Unit) {
    val isSelected = selected == label
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFFF9800) else Color(0xFFE0E0E0))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 24.dp)
    ) {
        Text(
            text = when (label) {
                "high" -> "매우 활동적"
                "moderate" -> "보통"
                else -> "비활동적"
            },
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
