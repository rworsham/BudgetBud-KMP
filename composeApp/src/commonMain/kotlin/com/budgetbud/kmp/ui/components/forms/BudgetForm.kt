package com.budgetbud.kmp.ui.components.forms

import com.budgetbud.kmp.auth.ApiClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.ui.components.AlertHandler
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun BudgetForm(
    modifier: Modifier = Modifier,
    apiClient: ApiClient,
    familyView: Boolean = false,
    onSuccess: () -> Unit,
) {
    var newBudget by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var existingBudgets by remember { mutableStateOf<List<String>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = apiClient.client.get("https://api.budgetingbud.com/api/budget/")
            val budgets = response.body<List<BudgetItem>>()
            existingBudgets = budgets.map { it.name }
        } catch (e: Exception) {
            errorMessage = "Failed to fetch existing budgets"
        } finally {
            isLoading = false
        }
    }

    fun validateForm(): Boolean {
        if (newBudget.isBlank() || amount.isBlank()) {
            errorMessage = "Please fill in all required fields"
            return false
        }

        if (existingBudgets.contains(newBudget)) {
            errorMessage = "Budget name already exists"
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
                apiClient.client.post("https://api.budgetingbud.com/api/budget/") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "name" to newBudget,
                            "total_amount" to amount
                        )
                    )
                }

                onSuccess()

            } catch (e: Exception) {
                errorMessage = "Failed to create new budget"
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
            value = newBudget,
            onValueChange = { newBudget = it },
            label = { Text("New Budget") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
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

@kotlinx.serialization.Serializable
data class BudgetItem(val name: String)
