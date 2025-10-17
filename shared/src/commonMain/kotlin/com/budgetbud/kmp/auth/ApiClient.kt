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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

expect fun provideHttpClientEngine(): HttpClientEngine

class ApiClient(private val tokenStorage: TokenStorage) {

    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val json = Json { ignoreUnknownKeys = true }

    val client = HttpClient(provideHttpClientEngine()) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val tokens = tokenStorage.getTokens()
                    println("loadTokens called, tokens = $tokens")
                    tokens?.let {
                        BearerTokens(it.accessToken, it.refreshToken ?: "")
                    }
                }

                refreshTokens {
                    println("Token Refresh initiated")
                    val current = tokenStorage.getTokens()
                    println("Current tokens: $current")

                    if (current?.refreshToken.isNullOrEmpty()) {
                        println("No refresh token found — returning null")
                        tokenStorage.clearTokens()
                        return@refreshTokens null
                    }

                    try {
                        val response = postRefreshToken(current.refreshToken)
                        println("Refresh response: $response")

                        val newAccess = response.access
                        val newRefresh = response.refresh ?: current.refreshToken

                        tokenStorage.saveTokens(TokenPair(newAccess, newRefresh))
                        println("Token refresh successful")

                        BearerTokens(newAccess, newRefresh)
                    } catch (e: CancellationException) {
                        println("Token refresh cancelled: ${e.message}")
                        throw e
                    } catch (e: Exception) {
                        println("Token refresh failed: ${e.message}")
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
        clientScope.launch {
            _isLoggedIn.value = tokenStorage.getTokens() != null
        }
    }

    suspend fun login(username: String, password: String): Boolean = withContext(clientScope.coroutineContext) {
        val response = client.post("https://api.budgetingbud.com/api/token/") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("username" to username, "password" to password))
        }

        if (response.status == HttpStatusCode.OK) {
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
    ): Boolean = withContext(clientScope.coroutineContext) {
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
        response.status == HttpStatusCode.OK
    }

    private suspend fun postRefreshToken(refresh: String): AuthTokens = withContext(clientScope.coroutineContext) {
        val response = client.post("https://api.budgetingbud.com/api/token/refresh/") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("refresh" to refresh))
        }

        if (!response.status.isSuccess()) throw Exception("Refresh failed")
        _isLoggedIn.value = true
        json.decodeFromString(response.bodyAsText())
    }

    suspend fun getUser(): User = withContext(clientScope.coroutineContext) {
        client.get("https://api.budgetingbud.com/api/user/").body()
    }

    suspend fun logout() = withContext(clientScope.coroutineContext) {
        tokenStorage.clearTokens()
        _isLoggedIn.value = false
    }

    suspend fun getTokens(): TokenPair? = withContext(clientScope.coroutineContext) {
        tokenStorage.getTokens()
    }
}
