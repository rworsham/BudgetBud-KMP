package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class TransactionPieChartData(
    val name: String,
    val value: String
)