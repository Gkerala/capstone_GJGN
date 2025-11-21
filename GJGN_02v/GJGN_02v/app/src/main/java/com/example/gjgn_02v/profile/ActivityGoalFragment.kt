/**
 * ActivityGoalFragment
 * --------------------------------------
 * - 5단계: 활동량 + 목표 선택 화면
 * - RadioGroup 값 저장 후 다음 단계로 이동
 */

package com.example.gjgn_02v.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gjgn_02v.R

class ActivityGoalFragment : Fragment() {

    private lateinit var viewModel: ProfileSetupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_activity_goal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProvider(requireActivity())[ProfileSetupViewModel::class.java]

        // ─────────────────────────────
        // View 매핑
        // ─────────────────────────────
        val rgActivity = view.findViewById<RadioGroup>(R.id.rgActivity)
        val rgGoal = view.findViewById<RadioGroup>(R.id.rgGoal)
        val btnNext = view.findViewById<Button>(R.id.btnNextActivityGoal)

        // ─────────────────────────────
        // 기존 값 복원 (있으면)
        // ─────────────────────────────
        restoreActivity(rgActivity, viewModel.activityLevel)
        restoreGoal(rgGoal, viewModel.goalType)

        // ─────────────────────────────
        // 다음 버튼 클릭 시 저장 후 이동
        // ─────────────────────────────
        btnNext.setOnClickListener {

            val selectedActivityId = rgActivity.checkedRadioButtonId
            val selectedGoalId = rgGoal.checkedRadioButtonId

            if (selectedActivityId == -1 || selectedGoalId == -1) {
                return@setOnClickListener
            }

            // 활동량 → 1~5 단계 값 저장
            val activityLevel = when (selectedActivityId) {
                R.id.rbActivity1 -> 1
                R.id.rbActivity2 -> 2
                R.id.rbActivity3 -> 3
                R.id.rbActivity4 -> 4
                R.id.rbActivity5 -> 5
                else -> 1
            }
            viewModel.activityLevel = activityLevel

            // 목표 → String 값 저장
            val goalType = when (selectedGoalId) {
                R.id.rbGoalLose -> "lose"
                R.id.rbGoalMaintain -> "maintain"
                R.id.rbGoalGain -> "gain"
                else -> "maintain"
            }
            viewModel.goalType = goalType

            (activity as ProfileSetupActivity).nextPage()
        }
    }

    // ─────────────────────────────────────────
    // 기존 선택 항목 복원
    // ─────────────────────────────────────────
    private fun restoreActivity(rg: RadioGroup, value: Int?) {
        if (value == null) return

        val id = when (value) {
            1 -> R.id.rbActivity1
            2 -> R.id.rbActivity2
            3 -> R.id.rbActivity3
            4 -> R.id.rbActivity4
            5 -> R.id.rbActivity5
            else -> return
        }
        rg.check(id)
    }

    private fun restoreGoal(rg: RadioGroup, value: String?) {
        if (value == null) return

        val id = when (value) {
            "lose" -> R.id.rbGoalLose
            "maintain" -> R.id.rbGoalMaintain
            "gain" -> R.id.rbGoalGain
            else -> return
        }
        rg.check(id)
    }
}
