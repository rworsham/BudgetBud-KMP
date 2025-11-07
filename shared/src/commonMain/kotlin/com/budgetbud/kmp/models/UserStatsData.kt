package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class UserStatsData(
    val total_transactions: Int,
    val joined_date: String,
    val savings_goals_met: Int,
    val net_balance: String? = null,
    val net_income: String? = null,
    val net_expense: String? = null
)