package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.auth.ApiClient

@Composable
expect fun IncomeExpenseBarChart(
    startDate: String,
    endDate: String,
    xSizePercent: Int,
    ySizePercent: Int,
    familyView: Boolean,
    modifier: Modifier,
    apiClient: ApiClient
)