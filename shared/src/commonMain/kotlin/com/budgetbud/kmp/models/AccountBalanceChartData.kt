package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class AccountBalanceChartData(
    val accounts: List<AccountData>,
    val history: List<AccountOverviewData>,
    val dataMax: Double
)