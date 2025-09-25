package com.budgetbud.kmp.auth

import com.budgetbud.kmp.auth.models.AuthTokens
import com.budgetbud.kmp.auth.models.User
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

expect fun provideHttpClientEngine(): HttpClientEngine

class ApiClient(private val tokenStorage: TokenStorage) {
    private val json = Json { ignoreUnknownKeys = true }

    val client = HttpClient(provideHttpClientEngine()) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Auth) {
            bearer {
                loadTokens {
                    tokenStorage.getTokens()?.let {
                        BearerTokens(it.accessToken, it.refreshToken)
                    }
                }
                refreshTokens {
                    val current = tokenStorage.getTokens()
                    if (current?.refreshToken.isNullOrEmpty()) return@refreshTokens null

                    try {
                        val response = postRefreshToken(current.refreshToken)
                        tokenStorage.saveTokens(TokenPair(response.access, response.refresh))
                        BearerTokens(response.access, response.refresh)
                    } catch (e: Exception) {
                        tokenStorage.clearTokens()
                        null
                    }
                }
            }
        }
    }

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    init {
        CoroutineScope(Dispatchers.Default).launch {
            _isLoggedIn.value = tokenStorage.getTokens() != null
        }
    }

    suspend fun login(username: String, password: String): Boolean {
        val response = client.post("https://api.budgetingbud.com/api/token/") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("username" to username, "password" to password))
        }

        return if (response.status == HttpStatusCode.OK) {
            val tokens = json.decodeFromString<AuthTokens>(response.bodyAsText())
            tokenStorage.saveTokens(TokenPair(tokens.access, tokens.refresh))
            _isLoggedIn.value = true
            true
        } else {
            false
        }
    }

    suspend fun register(
        email: String,
        username: String,
        firstName: String,
        lastName: String,
        password: String,
        token: String? = null
    ): Boolean {
        val response = client.post("https://api.budgetingbud.com/api/user/create/") {
            contentType(ContentType.Application.Json)
            setBody(
                buildMap {
                    put("email", email)
                    put("username", username)
                    put("first_name", firstName)
                    put("last_name", lastName)
                    put("password", password)
                    if (!token.isNullOrBlank()) {
                        put("token", token)
                    }
                }
            )
        }

        return response.status == HttpStatusCode.OK
    }

    private suspend fun postRefreshToken(refresh: String): AuthTokens {
        val response = client.post("https://api.budgetingbud.com/api/token/refresh/") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("refresh" to refresh))
        }

        if (!response.status.isSuccess()) throw Exception("Refresh failed") else _isLoggedIn.value = true
        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun getUser(): User {
        val response = client.get("https://api.budgetingbud.com/api/user/")
        return response.body()
    }

    suspend fun logout() {
        tokenStorage.clearTokens()
        _isLoggedIn.value = false
    }

    suspend fun getTokens(): TokenPair? {
        return tokenStorage.getTokens()
    }
}
