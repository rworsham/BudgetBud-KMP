package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class TransactionBarChartData(
    val category: String,
    val total_amount: String
)
