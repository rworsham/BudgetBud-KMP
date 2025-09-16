package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.models.BudgetRemainingBudgetBarChartData

@Composable
expect fun BudgetRemainingBarChart(
    data: List<BudgetRemainingBudgetBarChartData>,
    modifier: Modifier = Modifier
)