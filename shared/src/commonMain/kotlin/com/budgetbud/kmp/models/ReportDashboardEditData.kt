package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class ReportDashboardEditData(
    val id: Int,
    val display_name: String,
    val x_size: Int,
    val y_size: Int
)