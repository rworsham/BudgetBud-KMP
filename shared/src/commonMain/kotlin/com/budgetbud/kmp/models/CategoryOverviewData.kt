package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class CategoryOverviewData(
    val id: Int,
    val name: String,
    val balance: Double
)