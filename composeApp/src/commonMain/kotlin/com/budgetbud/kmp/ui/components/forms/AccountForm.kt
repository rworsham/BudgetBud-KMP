package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.AccountData
import com.budgetbud.kmp.ui.components.AlertHandler
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AccountForm(
    modifier: Modifier = Modifier,
    apiClient: ApiClient,
    onSuccess: () -> Unit,
) {
    var newAccount by remember { mutableStateOf("") }
    var newAccountBalance by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var existingAccounts by remember { mutableStateOf<List<AccountData>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val tokens = apiClient.getTokens()
            val response: HttpResponse = apiClient.client.get("https://api.budgetingbud.com/api/accounts/") {
                headers {
                    tokens?.let {
                        append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                    }
                }
            }
            existingAccounts = response.body()
        } catch (e: Exception) {
            errorMessage = "Failed to fetch existing accounts"
        } finally {
            isLoading = false
        }
    }

    fun validateForm(): Boolean {
        if (newAccount.isBlank()) {
            errorMessage = "Please fill in all required fields"
            return false
        }

        val accountExists = existingAccounts.any { it.name == newAccount }
        if (accountExists) {
            errorMessage = "Account name already exists"
            return false
        }

        return true
    }

    fun submitForm() {
        if (!validateForm()) return

        isSubmitting = true
        errorMessage = null

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val tokens = apiClient.getTokens()

                apiClient.client.post("https://api.budgetingbud.com/api/accounts/") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "name" to newAccount,
                            "balance" to newAccountBalance
                        )
                    )
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }

                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to create new Account. Please try again"
            } finally {
                isSubmitting = false
            }
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = newAccount,
            onValueChange = { newAccount = it },
            label = { Text("New Account") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = newAccountBalance,
            onValueChange = { newAccountBalance = it },
            label = { Text("Balance") },
            leadingIcon = { Text("$") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { submitForm() },
            enabled = !isLoading && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Submitting..." else "Submit")
        }

        errorMessage?.let {
            AlertHandler(alertMessage = it)
        }
    }
}
