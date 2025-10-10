package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class AccountOverviewData(
    val name: String,
    val balances: Map<String, String?> = emptyMap()
)