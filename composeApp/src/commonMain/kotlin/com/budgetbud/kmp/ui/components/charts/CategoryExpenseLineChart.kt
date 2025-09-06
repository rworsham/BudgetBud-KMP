package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.auth.ApiClient

@Composable
expect fun CategoryExpenseLineChart(
    xSizePercent: Int,
    ySizePercent: Int,
    apiClient: ApiClient,
    familyView: Boolean,
    modifier: Modifier = Modifier
)