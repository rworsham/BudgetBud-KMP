package com.budgetbud.kmp.auth.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
    val access: String,
    val refresh: String
)
