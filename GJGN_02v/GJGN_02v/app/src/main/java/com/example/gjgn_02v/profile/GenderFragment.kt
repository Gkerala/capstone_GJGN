/**
 * GenderFragment
 * ----------------------
 * - 1단계: 성별 선택 화면
 * - 남성/여성 선택 후 다음 단계로 이동
 */

package com.example.gjgn_02v.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gjgn_02v.R
import com.google.android.material.button.MaterialButtonToggleGroup

class GenderFragment : Fragment() {

    private lateinit var viewModel: ProfileSetupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_gender, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // ViewModel 가져오기 (Activity 와 공유됨)
        viewModel = ViewModelProvider(requireActivity())[ProfileSetupViewModel::class.java]

        val toggle = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleGender)
        val btnNext = view.findViewById<Button>(R.id.btnNextGender)

        // 다음 버튼 클릭 시
        btnNext.setOnClickListener {

            // 선택된 성별 확인
            val gender = when (toggle.checkedButtonId) {
                R.id.btnMale -> "male"
                R.id.btnFemale -> "female"
                else -> null
            }

            // 선택되지 않았다면 안내
            if (gender == null) {
                Toast.makeText(requireContext(), "성별을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ViewModel에 성별 저장
            viewModel.gender = gender

            // 다음 단계로 이동
            (activity as ProfileSetupActivity).nextPage()
        }
    }
}
