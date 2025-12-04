package com.example.gjgn_02v.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView   // ★ 추가
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.api.RetrofitClient
import com.example.gjgn_02v.data.model.auth.FullProfileRequest
import com.example.gjgn_02v.data.model.auth.UserProfileResponse
import com.example.gjgn_02v.main.MainActivity
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileSetupViewModel

    private val fragments = listOf(
        GenderFragment(),
        BirthFragment(),
        BodyFragment(),
        TargetWeightFragment(),
        ActivityGoalFragment(),
        SummaryFragment()
    )

    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        viewModel = ViewModelProvider(this)[ProfileSetupViewModel::class.java]

        // ★★★★★ 뒤로가기 버튼 기능 추가 (기존 코드 변경 없음)
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            prevPage()
        }

        loadFragment(0)
    }

    fun nextPage() {
        if (currentIndex < fragments.size - 1) {
            currentIndex++
            Log.d("PROFILE_DEBUG", "nextPage → $currentIndex 단계 이동")
            loadFragment(currentIndex)
        } else {
            Log.d("PROFILE_DEBUG", "모든 단계를 완료 → finishProfileSetup() 실행")
            finishProfileSetup()
        }
    }

    fun prevPage() {
        if (currentIndex > 0) {
            currentIndex--
            Log.d("PROFILE_DEBUG", "prevPage → $currentIndex 단계 이동")
            loadFragment(currentIndex)
        }
    }

    private fun loadFragment(index: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.profileSetupContainer, fragments[index])
            .commit()
    }

    /** 최종 저장 */
    fun finishProfileSetup() {

        if (!validateAllFields()) {
            Log.e("PROFILE_DEBUG", "❌ 프로필 데이터 중 null 존재 → 저장 중단")
            return
        }

        val request = FullProfileRequest(
            gender = viewModel.gender!!,
            birth = viewModel.birth!!,
            height = viewModel.height!!,
            weight = viewModel.weight!!,
        )

        Log.d("PROFILE_DEBUG", "서버로 보낼 JSON = " + Gson().toJson(request))

        RetrofitClient.api.updateFullProfile(request)
            .enqueue(object : Callback<UserProfileResponse> {

                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    Log.d("PROFILE_DEBUG", "응답 코드 = ${response.code()}")

                    if (response.isSuccessful) {
                        Log.d("PROFILE_DEBUG", "프로필 저장 성공 → MainActivity 이동")
                        startActivity(Intent(this@ProfileSetupActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Log.e(
                            "PROFILE_DEBUG",
                            "프로필 저장 실패: ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Log.e("PROFILE_DEBUG", "네트워크 오류", t)
                }
            })
    }

    /** null 값 검증 */
    private fun validateAllFields(): Boolean {
        return viewModel.gender != null &&
                viewModel.birth != null &&
                viewModel.height != null &&
                viewModel.weight != null &&
                viewModel.targetWeight != null &&
                viewModel.activityLevel != null &&
                viewModel.goalType != null
    }
}
