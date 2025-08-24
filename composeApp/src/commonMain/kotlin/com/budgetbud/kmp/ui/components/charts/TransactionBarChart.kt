package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import com.budgetbud.kmp.models.TransactionBarChartData
import androidx.compose.ui.Modifier


@Composable
expect fun TransactionBarChart(
    data: List<TransactionBarChartData>,
    modifier: Modifier = Modifier
)