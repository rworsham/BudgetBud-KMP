package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.models.CategoryHistoryLineChartData
import com.budgetbud.kmp.models.CategoryOverviewData

@Composable
expect fun CategoryLineChart(
    historyData: List<CategoryHistoryLineChartData>,
    categoryData: List<CategoryOverviewData>,
    modifier: Modifier
)