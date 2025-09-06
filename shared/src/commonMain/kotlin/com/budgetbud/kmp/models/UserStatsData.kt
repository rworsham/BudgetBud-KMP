package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class UserStatsData(
    val totalTransactions: String,
    val joinedDate: String,
    val savingsGoalsMet: String,
    val netBalance: String,
    val netIncome: String,
    val netExpense: String
)