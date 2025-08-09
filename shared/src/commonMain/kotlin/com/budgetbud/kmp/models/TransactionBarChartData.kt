package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class TransactionBarChartData(
    val category: CategoryData,
    val total_amount: String
)
