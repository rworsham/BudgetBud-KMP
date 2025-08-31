package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class ReportDashboardEditData(
    val id: String,
    val display_name: String,
    val x_size: String,
    val y_size: String
)