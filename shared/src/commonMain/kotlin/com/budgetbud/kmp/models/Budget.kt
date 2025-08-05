package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: String,
    val name: String
)
