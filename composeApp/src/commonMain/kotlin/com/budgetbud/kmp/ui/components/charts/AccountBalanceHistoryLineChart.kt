package com.budgetbud.kmp.ui.components.charts


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.auth.ApiClient

@Composable
expect fun AccountBalanceHistoryLineChart(
    apiClient: ApiClient,
    familyView: Boolean,
    modifier: Modifier = Modifier,
)