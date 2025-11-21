package com.example.gjgn_02v.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.model.mypage.EditActivityLevelActivity
import com.example.gjgn_02v.data.model.mypage.EditBirthActivity
import com.example.gjgn_02v.data.model.mypage.EditGenderActivity
import com.example.gjgn_02v.data.model.mypage.EditHeightActivity
import com.example.gjgn_02v.data.model.mypage.EditNameActivity
import com.example.gjgn_02v.data.model.mypage.EditWeightActivity
import com.example.gjgn_02v.data.model.mypage.EditGoalTypeActivity
import com.example.gjgn_02v.data.model.mypage.EditGoalWeightActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var btnEditName: Button
    private lateinit var btnEditBirth: Button
    private lateinit var btnEditGender: Button
    private lateinit var btnEditHeight: Button
    private lateinit var btnEditWeight: Button
    private lateinit var btnEditActivity: Button

    // ⭐ 추가된 버튼
    private lateinit var btnEditGoalType: Button
    private lateinit var btnEditGoalWeight: Button

    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        initViews()
        setupListeners()
        setupBottomNav()
    }

    private fun initViews() {
        btnEditName = findViewById(R.id.btnEditName)
        btnEditBirth = findViewById(R.id.btnEditBirth)
        btnEditGender = findViewById(R.id.btnEditGender)
        btnEditHeight = findViewById(R.id.btnEditHeight)
        btnEditWeight = findViewById(R.id.btnEditWeight)
        btnEditActivity = findViewById(R.id.btnEditActivity)

        // ⭐ 목표 유형 / 목표 체중 수정 버튼
        btnEditGoalType = findViewById(R.id.btnEditGoalType)
        btnEditGoalWeight = findViewById(R.id.btnEditGoalWeight)

        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {

        btnEditName.setOnClickListener {
            startActivity(Intent(this, EditNameActivity::class.java))
        }

        btnEditBirth.setOnClickListener {
            startActivity(Intent(this, EditBirthActivity::class.java))
        }

        btnEditGender.setOnClickListener {
            startActivity(Intent(this, EditGenderActivity::class.java))
        }

        btnEditHeight.setOnClickListener {
            startActivity(Intent(this, EditHeightActivity::class.java))
        }

        btnEditWeight.setOnClickListener {
            startActivity(Intent(this, EditWeightActivity::class.java))
        }

        btnEditActivity.setOnClickListener {
            startActivity(Intent(this, EditActivityLevelActivity::class.java))
        }

        // ⭐ 목표 유형 수정
        btnEditGoalType.setOnClickListener {
            startActivity(Intent(this, EditGoalTypeActivity::class.java))
        }

        // ⭐ 목표 체중 수정
        btnEditGoalWeight.setOnClickListener {
            startActivity(Intent(this, EditGoalWeightActivity::class.java))
        }

        // 뒤로가기
        btnBack.setOnClickListener { finish() }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_mypage

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main ->
                    startActivity(Intent(this, MainActivity::class.java))

                R.id.menu_record ->
                    startActivity(Intent(this, RecordSelectActivity::class.java))

                R.id.menu_analysis ->
                    startActivity(Intent(this, AnalysisActivity::class.java))

                R.id.menu_mypage ->
                    finish()
            }
            true
        }
    }
}
