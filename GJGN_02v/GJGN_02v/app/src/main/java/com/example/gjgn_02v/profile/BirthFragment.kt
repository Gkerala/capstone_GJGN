/**
 * BirthFragment
 * ---------------------------
 * - 2단계: 생년월일 선택 화면
 * - XML 내 DatePicker 사용
 * - 선택된 날짜를 ViewModel에 저장
 */

package com.example.gjgn_02v.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gjgn_02v.R
import java.text.SimpleDateFormat
import java.util.*

class BirthFragment : Fragment() {

    private lateinit var viewModel: ProfileSetupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_birth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // ViewModel 가져오기
        viewModel = ViewModelProvider(requireActivity())[ProfileSetupViewModel::class.java]

        val datePicker = view.findViewById<DatePicker>(R.id.datePickerBirth)
        val btnNext = view.findViewById<Button>(R.id.btnNextBirth)

        // DatePicker 기본값 설정 (이미 선택된 값이 있으면 불러오기)
        if (!viewModel.birth.isNullOrEmpty()) {
            val parts = viewModel.birth!!.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()

            datePicker.updateDate(year, month, day)
        }

        // 다음 버튼 클릭
        btnNext.setOnClickListener {

            // 날짜 읽기
            val year = datePicker.year
            val month = datePicker.month + 1
            val day = datePicker.dayOfMonth

            // yyyy-MM-dd 형식으로 저장
            val formatted = "%04d-%02d-%02d".format(year, month, day)

            viewModel.birth = formatted

            // 다음 페이지 이동
            (activity as ProfileSetupActivity).nextPage()
        }
    }
}
