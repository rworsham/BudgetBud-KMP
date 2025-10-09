package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class FamilyData(
    val id: Int,
    val name: String
)