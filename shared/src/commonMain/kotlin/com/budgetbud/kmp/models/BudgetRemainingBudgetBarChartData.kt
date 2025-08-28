package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
class BudgetRemainingBudgetBarChartData(
    val name : String,
    val starting_budget : Float,
    val remaining_budget : Float,
)
