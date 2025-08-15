package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class CategoryHistoryLineChartData(
    val name: String,
    val balances: Map<String, String?> = emptyMap()
)