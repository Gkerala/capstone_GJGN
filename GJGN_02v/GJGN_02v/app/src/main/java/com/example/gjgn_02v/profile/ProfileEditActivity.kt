package com.example.gjgn_02v.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.gjgn_02v.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.gjgn_02v.data.model.mypage.*
import com.google.android.material.textfield.TextInputEditText

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var btnEditName: Button
    private lateinit var btnEditBirth: Button
    private lateinit var btnEditGender: Button
    private lateinit var btnEditHeight: Button
    private lateinit var btnEditWeight: Button
    private lateinit var btnEditActivity: Button
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
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {

        // 이름 수정
        btnEditName.setOnClickListener {
            startActivity(Intent(this, EditNameActivity::class.java))
        }

        // 생년월일 수정
        btnEditBirth.setOnClickListener {
            startActivity(Intent(this, EditBirthActivity::class.java))
        }

        // 성별 수정
        btnEditGender.setOnClickListener {
            startActivity(Intent(this, EditGenderActivity::class.java))
        }

        // 키 수정
        btnEditHeight.setOnClickListener {
            startActivity(Intent(this, EditHeightActivity::class.java))
        }

        // 몸무게 수정
        btnEditWeight.setOnClickListener {
            startActivity(Intent(this, EditWeightActivity::class.java))
        }

        // 활동량 수정
        btnEditActivity.setOnClickListener {
            startActivity(Intent(this, EditActivityLevelActivity::class.java))
        }

        // 뒤로가기 → 마이페이지로 이동
        btnBack.setOnClickListener {
            finish()
        }
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
                    finish()   // 이미 내 정보 페이지 있으므로 닫기
            }
            true
        }
    }
    class EditNameActivity : AppCompatActivity() {

        private lateinit var etName: TextInputEditText
        private lateinit var btnSave: Button
        private lateinit var btnBack: Button

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_edit_name)

            etName = findViewById(R.id.etName)
            btnSave = findViewById(R.id.btnSave)
            btnBack = findViewById(R.id.btnBack)

            btnSave.setOnClickListener {
                val newName = etName.text.toString().trim()
                if (newName.isEmpty()) return@setOnClickListener
                updateProfile("name", newName)
            }

            btnBack.setOnClickListener { finish() }

            setupBottomNav()
        }

        private fun updateProfile(key: String, value: String) {
            RetrofitClient.api.updateUserInfo(mapOf(key to value))
                .enqueue(object : Callback<UserResponse> {
                    override fun onResponse(call: Call<UserResponse>, res: Response<UserResponse>) {
                        finish()
                    }
                    override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                        finish()
                    }
                })
        }

        private fun setupBottomNav() {
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.selectedItemId = R.id.menu_mypage
            bottomNav.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.menu_main -> startActivity(Intent(this, MainActivity::class.java))
                    R.id.menu_record -> startActivity(Intent(this, RecordSelectActivity::class.java))
                    R.id.menu_analysis -> startActivity(Intent(this, AnalysisActivity::class.java))
                    R.id.menu_mypage -> finish()
                }
                true
            }
        }
    }

}
