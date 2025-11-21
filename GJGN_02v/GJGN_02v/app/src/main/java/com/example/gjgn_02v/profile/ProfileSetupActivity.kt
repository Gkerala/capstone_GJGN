/**
 * ProfileSetupActivity
 * ------------------------------------------------------
 * - 6단계 프로필 설정 전체 흐름을 관리하는 Activity
 * - nextPage(), prevPage() 로 단계 이동
 * - 모든 입력값은 ProfileSetupViewModel 에 저장됨
 */

package com.example.gjgn_02v.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.gjgn_02v.main.MainActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.model.auth.UserProfileResponse

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileSetupViewModel

    /**
     * 6단계 Fragment 리스트
     */
    private val fragments = listOf(
        GenderFragment(),          // 1단계
        BirthFragment(),           // 2단계
        BodyFragment(),            // 3단계
        TargetWeightFragment(),    // 4단계
        ActivityGoalFragment(),    // 5단계
        SummaryFragment()          // 6단계 (최종 확인)
    )

    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this)[ProfileSetupViewModel::class.java]

        // 첫 번째 화면 표시 (성별)
        loadFragment(0)
    }

    /**
     * 다음 단계 이동
     */
    fun nextPage() {
        if (currentIndex < fragments.size - 1) {
            currentIndex++
            loadFragment(currentIndex)
        }
    }

    /**
     * 이전 단계 이동
     */
    fun prevPage() {
        if (currentIndex > 0) {
            currentIndex--
            loadFragment(currentIndex)
        }
    }

    /**
     * Fragment 화면 전환 함수
     */
    private fun loadFragment(index: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.profileSetupContainer, fragments[index])
            .commit()
    }

    /**
     * 6단계 Summary 화면에서 호출
     * → 모든 정보 확인 후 실제 저장 로직 실행
     */
    fun finishProfileSetup() {

        val request = FullProfileRequest(
            gender = viewModel.gender!!,
            birth = viewModel.birth!!,
            height = viewModel.height!!,
            weight = viewModel.weight!!,
            target_weight = viewModel.targetWeight!!,
            activity_level = viewModel.activityLevel!!,
            goal_type = viewModel.goalType!!
        )

        ApiClient.apiService.updateFullProfile(request)
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        startActivity(Intent(this@ProfileSetupActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Log.e("ProfileSave", "Fail: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Log.e("ProfileSave", "Network Error", t)
                }
            })
    }

}
