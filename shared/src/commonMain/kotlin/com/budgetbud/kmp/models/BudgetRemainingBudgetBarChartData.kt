package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
class BudgetRemainingBudgetBarChartData(
    val name : String,
    val starting_budget : Double,
    val remaining_budget : Double,
)
