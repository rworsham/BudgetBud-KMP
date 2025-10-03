package com.budgetbud.kmp.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.*
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.ui.components.Dashboard
import com.budgetbud.kmp.ui.components.LoginScreen
import com.budgetbud.kmp.ui.components.SignUpScreen

@Composable
actual fun AppNavigation(apiClient: ApiClient) {
    val navController = rememberNavController()
    val isLoggedIn by apiClient.isLoggedIn.collectAsState()

    val startDestination = if (isLoggedIn) "dashboard" else "login"

    Log.d("AppNavigation", "AppNavigation Composable called")

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                loginUser = { username, password, _ ->
                    val success = apiClient.login(username, password)
                    val savedTokens = apiClient.getTokens()
                    Log.d("Login", "Verified saved tokens before navigating: $savedTokens")
                    if (success) Result.success(Unit) else Result.failure(Exception("Login failed"))
                }
            )
        }

        composable("dashboard") {
            Dashboard(apiClient = apiClient)
        }

        composable("signup") {
            SignUpScreen(
                navController = navController,
                token = null,
                createUser = { email, username, firstName, lastName, password, token ->
                    val success = apiClient.register(
                        email = email,
                        username = username,
                        firstName = firstName,
                        lastName = lastName,
                        password = password,
                        token = token
                    )
                    if (success) Result.success(Unit) else Result.failure(Exception("Registration failed"))
                }
            )
        }
    }
}