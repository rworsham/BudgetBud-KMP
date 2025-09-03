package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class FamilyCategoryOverviewData(
    val name: String,
    val category: String,
    val category_count: Float
)