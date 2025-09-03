package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.auth.ApiClient

@Composable
expect fun CategoryUsagePerUserBarChart(
    startDate: String,
    endDate: String,
    xSizePercent: Int,
    ySizePercent: Int,
    familyView: Boolean,
    apiClient: ApiClient,
    modifier: Modifier
)