package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class UserDetailsData (
    val id: Int,
    val username: String,
    val email: String
)