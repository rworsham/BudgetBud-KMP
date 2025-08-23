package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class TransactionHistoryTableData(
    val id: Int,
    val amount: String,
    val description: String,
    val budget: String,
    val category: String,
    val date: String,
    val account: String,
    val transaction_type: String,
    val is_recurring: String,
)