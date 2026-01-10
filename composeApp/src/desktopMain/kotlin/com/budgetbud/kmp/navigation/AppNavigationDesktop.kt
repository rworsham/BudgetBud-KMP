package com.budgetbud.kmp.navigation

import androidx.compose.runtime.*
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.ui.components.Dashboard
import com.budgetbud.kmp.ui.components.LoginScreenDesktop
import com.budgetbud.kmp.ui.components.SignUpScreenDesktop
import kotlinx.coroutines.launch


sealed class Screen {
    object Login : Screen()
    object Dashboard : Screen()
    object SignUp : Screen()
}

@Composable
actual fun AppNavigation(apiClient: ApiClient) {
    val isLoggedIn by apiClient.isLoggedIn.collectAsState()

    var currentScreen by remember {
        mutableStateOf(
            if (isLoggedIn) Screen.Dashboard else Screen.Login
        )
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isLoggedIn) {
        currentScreen = if (isLoggedIn) {
            Screen.Dashboard
        } else {
            Screen.Login
        }
    }

    when (currentScreen) {

        Screen.Login -> {
            LoginScreenDesktop(
                loginUser = { username, password, _ ->
                    coroutineScope.launch {
                        val success = apiClient.login(username, password)
                        if (success) {
                            currentScreen = Screen.Dashboard
                            Result.success(Unit)
                        } else {
                            Result.failure(Exception("Login failed"))
                        }
                    }
                },
                onSignUp = {
                    currentScreen = Screen.SignUp
                }
            )
        }

        Screen.Dashboard -> {
            Dashboard(apiClient = apiClient)
        }

        Screen.SignUp -> {
            SignUpScreen(
                token = null,
                createUser = { email, username, firstName, lastName, password, token ->
                    coroutineScope.launch {
                        val success = apiClient.register(
                            email = email,
                            username = username,
                            firstName = firstName,
                            lastName = lastName,
                            password = password,
                            token = token
                        )
                        if (success) {
                            currentScreen = Screen.Login
                            Result.success(Unit)
                        } else {
                            Result.failure(Exception("Registration failed"))
                        }
                    }
                }
            )
        }
    }
}
