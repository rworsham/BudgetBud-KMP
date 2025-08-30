package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseCategoriesPieChartData(
    val name: String,
    val value: String
)