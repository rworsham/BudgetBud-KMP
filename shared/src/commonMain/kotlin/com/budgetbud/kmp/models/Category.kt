package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String
)
