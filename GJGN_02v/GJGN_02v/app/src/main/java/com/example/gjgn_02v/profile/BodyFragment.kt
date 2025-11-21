/**
 * BodyFragment
 * --------------------------------------
 * - 3단계: 키 & 몸무게 입력 화면
 * - NumberPicker(100/10/1/0.1) 입력값을 Float 값으로 생성
 * - ViewModel(height, weight) 저장
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

class BodyFragment : Fragment() {

    private lateinit var viewModel: ProfileSetupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_body, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProvider(requireActivity())[ProfileSetupViewModel::class.java]

        // ─────────────────────────────
        // NumberPicker 연결 (키)
        // ─────────────────────────────
        val h100 = view.findViewById<NumberPicker>(R.id.npHeight100)
        val h10 = view.findViewById<NumberPicker>(R.id.npHeight10)
        val h1 = view.findViewById<NumberPicker>(R.id.npHeight1)
        val hDecimal = view.findViewById<NumberPicker>(R.id.npHeightDecimal)

        // ─────────────────────────────
        // NumberPicker 연결 (몸무게)
        // ─────────────────────────────
        val w100 = view.findViewById<NumberPicker>(R.id.npWeight100)
        val w10 = view.findViewById<NumberPicker>(R.id.npWeight10)
        val w1 = view.findViewById<NumberPicker>(R.id.npWeight1)
        val wDecimal = view.findViewById<NumberPicker>(R.id.npWeightDecimal)

        // ─────────────────────────────
        // NumberPicker 기본 설정
        // ─────────────────────────────
        setPickerRange(h100, 0, 2)
        setPickerRange(h10, 0, 9)
        setPickerRange(h1, 0, 9)
        setPickerRange(hDecimal, 0, 9)

        setPickerRange(w100, 0, 2)
        setPickerRange(w10, 0, 9)
        setPickerRange(w1, 0, 9)
        setPickerRange(wDecimal, 0, 9)

        // ─────────────────────────────
        // 기존 값 복구
        // ─────────────────────────────
        restoreToPicker(viewModel.height, h100, h10, h1, hDecimal)
        restoreToPicker(viewModel.weight, w100, w10, w1, wDecimal)

        // 버튼 연결
        val btnNext = view.findViewById<Button>(R.id.btnNextBody)

        btnNext.setOnClickListener {

            // 입력값 → Float 변환
            val height = toDecimalFloat(h100.value, h10.value, h1.value, hDecimal.value)
            val weight = toDecimalFloat(w100.value, w10.value, w1.value, wDecimal.value)

            // ViewModel 저장
            viewModel.height = height
            viewModel.weight = weight

            // 다음 화면 이동
            (activity as ProfileSetupActivity).nextPage()
        }
    }

    // ─────────────────────────────────────────
    // NumberPicker 설정 함수
    // ─────────────────────────────────────────
    private fun setPickerRange(picker: NumberPicker, min: Int, max: Int) {
        picker.minValue = min
        picker.maxValue = max
        picker.wrapSelectorWheel = true
    }

    // ─────────────────────────────────────────
    // NumberPicker 값 → Float 계산
    // ex) 1 7 5 . 3 → 175.3
    // ─────────────────────────────────────────
    private fun toDecimalFloat(v100: Int, v10: Int, v1: Int, decimal: Int): Float {
        val num = (v100 * 100) + (v10 * 10) + v1 + (decimal / 10f)
        return "%.1f".format(num).toFloat()
    }

    // ─────────────────────────────────────────
    // 기존 ViewModel 값 → NumberPicker 복원
    // ─────────────────────────────────────────
    private fun restoreToPicker(
        value: Float?,
        p100: NumberPicker, p10: NumberPicker, p1: NumberPicker, pDec: NumberPicker
    ) {
        if (value == null) return

        val str = "%.1f".format(value) // ex: 175.3
        val parts = str.split(".")     // ["175", "3"]

        val whole = parts[0].padStart(3, '0') // "175"
        val decimal = parts[1].toInt()

        p100.value = whole[0].digitToInt()
        p10.value = whole[1].digitToInt()
        p1.value = whole[2].digitToInt()
        pDec.value = decimal
    }
}
