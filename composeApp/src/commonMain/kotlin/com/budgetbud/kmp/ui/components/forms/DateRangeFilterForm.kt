package com.budgetbud.kmp.ui.components.forms

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import androidx.compose.ui.Modifier

@Composable
expect fun DateRangeFilterForm(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
)