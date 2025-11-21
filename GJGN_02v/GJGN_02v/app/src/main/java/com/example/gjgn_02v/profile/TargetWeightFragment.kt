/**
 * TargetWeightFragment
 * -------------------------
 * - 4단계: 목표 체중 선택 화면
 * - NumberPicker 조합으로 소수점 입력 처리
 */

package com.example.gjgn_02v.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gjgn_02v.R

class TargetWeightFragment : Fragment() {

    private lateinit var viewModel: ProfileSetupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_target_weight, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProvider(requireActivity())[ProfileSetupViewModel::class.java]

        // NumberPickers 매핑
        val np100 = view.findViewById<NumberPicker>(R.id.npTarget100)
        val np10 = view.findViewById<NumberPicker>(R.id.npTarget10)
        val np1 = view.findViewById<NumberPicker>(R.id.npTarget1)
        val npDecimal = view.findViewById<NumberPicker>(R.id.npTargetDecimal)

        val btnNext = view.findViewById<Button>(R.id.btnNextTarget)

        // NumberPicker 기본 설정
        setupNumberPicker(np100, 0, 2)
        setupNumberPicker(np10, 0, 9)
        setupNumberPicker(np1, 0, 9)
        setupNumberPicker(npDecimal, 0, 9)

        // 다음 버튼 클릭
        btnNext.setOnClickListener {

            val weightInt =
                np100.value * 100 +
                        np10.value * 10 +
                        np1.value

            val weightDecimal = npDecimal.value * 0.1f

            val finalValue = weightInt + weightDecimal

            viewModel.targetWeight = finalValue

            (activity as ProfileSetupActivity).nextPage()
        }
    }

    private fun setupNumberPicker(np: NumberPicker, min: Int, max: Int) {
        np.minValue = min
        np.maxValue = max
        np.wrapSelectorWheel = true
    }
}
