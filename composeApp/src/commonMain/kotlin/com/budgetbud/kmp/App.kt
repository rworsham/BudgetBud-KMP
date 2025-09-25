package com.budgetbud.kmp

import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.budgetbud.kmp.navigation.AppNavigation
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.auth.tokenStorage

@Composable
@Preview
fun App() {
    val apiClient = ApiClient(tokenStorage)
    AppNavigation(apiClient)
}
