package com.budgetbud.kmp.navigation

import androidx.compose.runtime.Composable
import com.budgetbud.kmp.auth.ApiClient

@Composable
expect fun AppNavigation(
    apiClient: ApiClient
)
