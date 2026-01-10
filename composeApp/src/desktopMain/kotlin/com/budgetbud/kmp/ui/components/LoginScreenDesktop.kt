package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


@Composable
fun LoginScreenDesktop(
    token: String? = null,
    loginUser: suspend (username: String, password: String, token: String?) -> Result<Unit>,
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            GradientTitle(text = "BudgetBud", fontSize = 42.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    GradientTitle(text = "Log In", fontSize = 26.sp)

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isSubmitting = true
                            errorMessage = null

                            scope.launch {
                                val result = loginUser(username, password, token)
                                isSubmitting = false

                                if (result.isSuccess) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Login failed. Please check your credentials."
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isSubmitting) "Processing..." else "Login")
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        AlertHandler(it)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onNavigateToSignup) {
                Text("Need an account? Sign up here")
            }
        }
    }
}
