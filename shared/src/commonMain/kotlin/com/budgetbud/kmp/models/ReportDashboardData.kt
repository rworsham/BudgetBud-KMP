package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class ReportDashboardData(
    val displayName: String,
    val xSize: Int,
    val ySize: Int
)
