package com.budgetbud.kmp.auth

expect val tokenStorage: TokenStorage

data class TokenPair(val accessToken: String, val refreshToken: String? = null)

interface TokenStorage {
    suspend fun getTokens(): TokenPair?
    suspend fun saveTokens(tokens: TokenPair)
    suspend fun clearTokens()
}