package com.budgetbud.kmp.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.ui.components.Dashboard
import com.budgetbud.kmp.ui.components.LoginScreenDesktop
import com.budgetbud.kmp.ui.components.SignUpScreenDesktop

@Composable
actual fun AppNavigation(apiClient: ApiClient) {
    val navController = rememberNavController()
    val isLoggedIn by apiClient.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        println("DesktopNavigation: AppNavigation called — isLoggedIn=$isLoggedIn")

        if (isLoggedIn) {
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "dashboard" else "login"
    ) {
        composable("login") {
            LoginScreenDesktop(
                token = null,
                loginUser = { username, password, token ->
                    val success = apiClient.login(username, password)
                    if (success) Result.success(Unit) else Result.failure(Exception("Login failed"))
                },
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate("signup")
                }
            )
        }

        composable("dashboard") {
            Dashboard(apiClient = apiClient)
        }

        composable("signup") {
            SignUpScreenDesktop(
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