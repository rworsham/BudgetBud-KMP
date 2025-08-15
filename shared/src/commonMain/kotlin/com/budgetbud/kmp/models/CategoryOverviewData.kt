package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class CategoryOverviewData(
    val id: String,
    val name: String,
    val balance: String
)