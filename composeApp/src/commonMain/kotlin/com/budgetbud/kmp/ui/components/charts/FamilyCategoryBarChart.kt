package com.budgetbud.kmp.ui.components.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.models.FamilyCategoryOverviewData

@Composable
expect fun FamilyCategoryBarChart(
    data: List<FamilyCategoryOverviewData>,
    modifier: Modifier = Modifier
)