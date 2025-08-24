package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import com.budgetbud.kmp.models.TransactionPieChartData
import androidx.compose.ui.Modifier


@Composable
expect fun TransactionPieChart(
    data: List<TransactionPieChartData>,
    modifier: Modifier = Modifier
)