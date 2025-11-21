package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class ReportDashboardData(
    val display_name: String,
    val x_size: String,
    val y_size: String
)
