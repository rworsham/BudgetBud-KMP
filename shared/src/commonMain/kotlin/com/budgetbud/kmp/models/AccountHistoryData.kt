package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class AccountHistoryData(
    val account_id : Int,
    val start_date: String,
    val end_date : String
)