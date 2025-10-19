package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class TransactionTableData(
    val id: Int,
    val amount: String,
    val description: String,
    val budget: String,
    val category: String,
    val account: String,
    val date: String,
    val transaction_type: String,
    val is_recurring: Boolean,
    val next_occurrence: String? = null
)