package com.example.gjgn_02v.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

// 🔥 Light Mode 컬러 정의 (XML colors.xml과 동일하게 유지 가능)
private val LightColorScheme = lightColorScheme(
    primary = Teal700,
    primaryContainer = Teal700,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    background = Color.White,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// 🔥 (필요시) Night Mode 컬러도 정의 가능. 하지만 지금은 Light만 사용.
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

@Composable
fun GJGN_02vTheme(
    darkTheme: Boolean = false,           // 🔥 다크모드 강제 OFF
    dynamicColor: Boolean = false,        // 🔥 동적테마 강제 OFF
    content: @Composable () -> Unit
) {

    val colorScheme = LightColorScheme    // 🔥 무조건 LightColorScheme 사용

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
