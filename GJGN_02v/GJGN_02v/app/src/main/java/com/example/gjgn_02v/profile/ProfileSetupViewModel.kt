/**
 * ProfileSetupViewModel
 * ----------------------
 * - 프로필 설정 단계에서 입력받은 모든 정보를 저장
 * - Activity 와 모든 Fragment 가 공유함
 */

package com.example.gjgn_02v.profile

import androidx.lifecycle.ViewModel

class ProfileSetupViewModel : ViewModel() {

    // 1단계
    var gender: String? = null

    // 2단계
    var birth: String? = null   // yyyy-MM-dd

    // 3단계
    var height: Float? = null
    var weight: Float? = null

    // 4단계
    var targetWeight: Float? = null

    // 5단계
    var activityLevel: Int? = null   // 1~5단계
    var goalType: String? = null     // loss / maintain / gain
}
