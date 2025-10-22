package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class NewTransactionData(
    val date: String?,
    val amount: String,
    val transaction_type: String,
    val description: String? = null,
    val category: Long,
    val budget: Long,
    val account: Long,
    val is_recurring: Boolean,
    val recurring_type: String? = null,
    val next_occurrence: String? = null
)