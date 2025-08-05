package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: String,
    val name: String
)
