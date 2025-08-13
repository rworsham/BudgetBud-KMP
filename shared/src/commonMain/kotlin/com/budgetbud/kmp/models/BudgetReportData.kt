package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class BudgetReportData(
    val transactions: List<Transaction>,
    val budgets_remaining: List<BudgetSummary>
)

@Serializable
data class BudgetSummary(
    val budget_name: String,
    val starting_budget: String,
    val remaining_budget: String,
    val total_income: String,
    val total_expense: String
)
