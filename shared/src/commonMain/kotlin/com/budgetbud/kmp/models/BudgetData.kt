package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class BudgetData(
    val id: Int,
    val name: String,
    val totalAmount: String
)
