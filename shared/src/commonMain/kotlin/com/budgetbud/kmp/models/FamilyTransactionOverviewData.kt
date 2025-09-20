package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class FamilyTransactionOverviewData(
    val name: String,
    val transaction_count: Float
)