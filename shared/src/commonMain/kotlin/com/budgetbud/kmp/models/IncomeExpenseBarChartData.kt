package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class IncomeExpenseBarChartData(
    val name: String,
    val value: Float
)