package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.models.ExpenseCategoriesPieChartData

@Composable
expect fun ExpenseCategoriesBudgetPieChart(
    data: List<ExpenseCategoriesPieChartData>,
    modifier: Modifier = Modifier
)