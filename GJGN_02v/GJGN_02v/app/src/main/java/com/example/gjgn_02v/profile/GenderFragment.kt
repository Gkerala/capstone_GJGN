package com.example.gjgn_02v.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gjgn_02v.R

class GenderFragment : Fragment() {

    private lateinit var viewModel: ProfileSetupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_gender, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProvider(requireActivity())[ProfileSetupViewModel::class.java]

        val btnMale = view.findViewById<Button>(R.id.btnMale)
        val btnFemale = view.findViewById<Button>(R.id.btnFemale)
        val btnNext = view.findViewById<Button>(R.id.btnNextGender)

        fun updateUI(gender: String?) {
            val selectedColor = ContextCompat.getColor(requireContext(), R.color.purple_500)
            val unselectedColor = ContextCompat.getColor(requireContext(), R.color.gray_light)

            btnMale.setBackgroundColor(if (gender == "male") selectedColor else unselectedColor)
            btnFemale.setBackgroundColor(if (gender == "female") selectedColor else unselectedColor)
        }

        // 초기 UI 복원
        updateUI(viewModel.gender)

        btnMale.setOnClickListener {
            viewModel.gender = "male"
            updateUI("male")
        }

        btnFemale.setOnClickListener {
            viewModel.gender = "female"
            updateUI("female")
        }

        btnNext.setOnClickListener {
            if (viewModel.gender == null) {
                // 성별 선택 안함
                return@setOnClickListener
            }
            (activity as ProfileSetupActivity).nextPage()
        }
    }
}
