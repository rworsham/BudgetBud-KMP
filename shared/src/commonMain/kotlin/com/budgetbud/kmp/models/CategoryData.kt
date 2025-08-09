package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class CategoryData(
    val id: String,
    val name: String
)
