package com.budgetbud.kmp.navigation

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

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                loginUser = { username, password, _ ->
                    val success = apiClient.login(username, password)
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