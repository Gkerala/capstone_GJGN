package com.example.gjgn_02v

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainScreen() }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "메인화면",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            AndroidView(
                factory = { ctx ->
                    BottomNavigationView(ctx).apply {
                        inflateMenu(R.menu.bottom_nav_menu)
                        selectedItemId = R.id.menu_main
                        labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED

                        setOnItemSelectedListener { item ->
                            when (item.itemId) {
                                R.id.menu_main -> true
                                R.id.menu_record -> {
                                    activity?.startActivity(Intent(activity, RecordActivity::class.java))
                                    true
                                }
                                R.id.menu_analysis -> {
                                    activity?.startActivity(Intent(activity, AnalysisActivity::class.java))
                                    true
                                }
                                R.id.menu_mypage -> {
                                    activity?.startActivity(Intent(activity, MyPageActivity::class.java))
                                    true
                                }
                                else -> false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}
