package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.models.AccountBalanceChartData

@Composable
expect fun AccountBalanceLineChart(
    chartData: AccountBalanceChartData,
    modifier: Modifier = Modifier
)
