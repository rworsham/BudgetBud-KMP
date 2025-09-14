package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.models.IncomeExpenseBarChartData

@Composable
expect fun IncomeExpenseBudgetBarChart(
    data: List<IncomeExpenseBarChartData>,
    modifier: Modifier = Modifier
)