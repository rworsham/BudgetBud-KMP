package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class AccountData(
    val id: String,
    val name: String
)
