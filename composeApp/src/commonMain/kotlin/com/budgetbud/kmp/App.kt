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
private val BackgroundColor = Color(0xFFFFFFFF)
private val SurfaceColor = Color(0xFFF7F7F7)
private val TextPrimary = Color(0xFF000000)
private val TextSecondary = LightGray

private val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, color = TextSecondary)
)

private val AppColorScheme = lightColorScheme(
    primary = BBGreen,
    onPrimary = Color.White,
    secondary = BBGreen,
    onSecondary = Color.White,
    background = BackgroundColor,
    onBackground = TextPrimary,
    surface = SurfaceColor,
    onSurface = TextPrimary
)

@Composable
@Preview
fun App() {
    val apiClient = ApiClient(tokenStorage)

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography
    ) {
        AppNavigation(apiClient)
    }
}
