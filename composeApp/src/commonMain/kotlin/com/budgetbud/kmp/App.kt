package com.budgetbud.kmp

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.budgetbud.kmp.navigation.AppNavigation
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.auth.tokenStorage

private val BBGreen = Color(0xFF1DB954)
private val LightGray = Color(0xFFB3B3B3)
private val DarkGray = Color(0xFF121212)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, color = LightGray)
)

private val AppDarkColors = darkColorScheme(
    primary = BBGreen,
    onPrimary = Color.Black,
    secondary = BBGreen,
    onSecondary = Color.Black,
    background = DarkGray,
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

@Composable
@Preview
fun App() {
    val apiClient = remember { ApiClient(tokenStorage) }

    MaterialTheme(
        colorScheme = AppDarkColors,
        typography = AppTypography
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppNavigation(apiClient)
        }
    }
}