package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class FamilyData(
    val id: Long,
    val username: String,
    val email: String? = null
)