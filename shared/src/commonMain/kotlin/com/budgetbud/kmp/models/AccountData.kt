package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class AccountData(
    val id: Int,
    val name: String,
    val balance: String
)
