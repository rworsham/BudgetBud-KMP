package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.datetime.LocalDate


@Serializable
data class Transaction(
    val id: Long?,
    val date: LocalDate,
    val amount: String,
    val transaction_type: String,
    val description: String? = null,
    val category: Long,
    val budget: Long,
    val account: Long,
    val is_recurring: Boolean,
    val recurring_type: String? = null,
    val next_occurrence: LocalDate? = null,
    val user: Long? = null,
    val family: Long? = null
)
