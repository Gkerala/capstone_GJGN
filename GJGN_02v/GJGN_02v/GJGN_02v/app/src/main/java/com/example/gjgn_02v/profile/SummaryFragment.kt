package com.example.gjgn_02v.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gjgn_02v.R

class SummaryFragment : Fragment() {

    private lateinit var viewModel: ProfileSetupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProvider(requireActivity())[ProfileSetupViewModel::class.java]

        val tvGender = view.findViewById<TextView>(R.id.tvSummaryGender)
        val tvBirth = view.findViewById<TextView>(R.id.tvSummaryBirth)
        val tvHeight = view.findViewById<TextView>(R.id.tvSummaryHeight)
        val tvWeight = view.findViewById<TextView>(R.id.tvSummaryWeight)
        val tvTarget = view.findViewById<TextView>(R.id.tvSummaryTarget)
        val tvActivity = view.findViewById<TextView>(R.id.tvSummaryActivity)
        val tvGoal = view.findViewById<TextView>(R.id.tvSummaryGoal)
        val btnFinish = view.findViewById<Button>(R.id.btnFinish)

        // ★ 뒤로가기 버튼 추가 (기존 코드 변경 없음)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            (activity as ProfileSetupActivity).prevPage()
        }

        // 성별
        tvGender.text = "성별: ${viewModel.gender ?: "-"}"

        // 생년월일
        tvBirth.text = "생년월일: ${viewModel.birth ?: "-"}"

        // 키
        tvHeight.text = "키: ${viewModel.height ?: 0f} cm"

        // 몸무게
        tvWeight.text = "몸무게: ${viewModel.weight ?: 0f} kg"

        // 목표 체중
        tvTarget.text = "목표 체중: ${viewModel.targetWeight ?: 0f} kg"

        // 활동량
        val activityText = when (viewModel.activityLevel) {
            1 -> "거의 활동 없음"
            2 -> "적은 활동"
            3 -> "보통 활동"
            4 -> "많은 활동"
            5 -> "매우 많은 활동"
            else -> "-"
        }
        tvActivity.text = "활동량: $activityText"

        // 목표
        val goalText = when (viewModel.goalType) {
            "lose" -> "체중 감량"
            "maintain" -> "유지"
            "gain" -> "체중 증가"
            else -> "-"
        }
        tvGoal.text = "목표: $goalText"

        // 완료 버튼
        btnFinish.setOnClickListener {
            (activity as ProfileSetupActivity).finishProfileSetup()
        }
    }
}
