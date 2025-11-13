package com.example.gjgn_02v

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import android.widget.ScrollView


class AnalysisActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        // ✅ 스크롤뷰와 함께 사용 (activity_analysis.xml 안에 NestedScrollView로 감싸기)
        val scrollView = findViewById<ScrollView>(R.id.scrollView)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNav.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        bottomNav.selectedItemId = R.id.menu_analysis

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> startActivity(Intent(this, MainActivity::class.java))
                R.id.menu_record -> startActivity(Intent(this, RecordActivity::class.java))
                R.id.menu_mypage -> startActivity(Intent(this, MyPageActivity::class.java))
            }
            true
        }
    }
}
