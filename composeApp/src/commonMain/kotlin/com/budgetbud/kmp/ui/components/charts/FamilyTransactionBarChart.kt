package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.models.FamilyTransactionOverviewData

@Composable
expect fun FamilyTransactionBarChart(
    data: List<FamilyTransactionOverviewData>,
    modifier: Modifier = Modifier
)