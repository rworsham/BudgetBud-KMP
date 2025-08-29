package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseCategoryBarChartData(
    val category: String,
    val total_amount: Float
)