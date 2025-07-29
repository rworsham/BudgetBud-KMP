package com.budgetbud.kmp.auth

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun provideHttpClientEngine(): HttpClientEngine

class ApiClient(private val authManager: AuthManager, engine: HttpClientEngine) {

    private val client = HttpClient(provideHttpClientEngine()) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val tokens = tokenStorage.getTokens()
                    BearerTokens(
                        tokens?.accessToken.orEmpty(),
                        tokens?.refreshToken.orEmpty()
                    )
                }
                refreshTokens {
                    val newTokens = refreshTokensFromApi()
                    tokenStorage.saveTokens(newTokens.access, newTokens.refresh)
                    newTokens
                }
            }
        }
    }
    suspend fun getUserProfile(): UserProfile {
        return client.get("user/profile").body()
    }
}
