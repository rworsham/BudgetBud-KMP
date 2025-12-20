package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class BudgetHistoryData(
    val budget_id : Int,
    val start_date: String,
    val end_date : String
)