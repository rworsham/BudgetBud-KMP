package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.BudgetData
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.ui.components.AlertHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*

@Composable
fun BudgetEditForm(
    modifier: Modifier = Modifier,
    apiClient: ApiClient,
    onSuccess: () -> Unit
) {
    var selectedBudgetId by remember { mutableStateOf<Int?>(null) }
    var newName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var existingBudgetData by remember { mutableStateOf<List<BudgetData>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = apiClient.client.get("https://api.budgetingbud.com/api/budget/")
            existingBudgetData = response.body()
        } catch (e: Exception) {
            errorMessage = "Failed to fetch budgets"
        } finally {
            isLoading = false
        }
    }

    fun handleBudgetChange(budgetId: Int) {
        selectedBudgetId = budgetId
        val selected = existingBudgetData.find { it.id == budgetId }
        if (selected != null) {
            newName = selected.name
            amount = selected.totalAmount
        }
    }

    fun submitForm() {
        if (selectedBudgetId == null || newName.isBlank() || amount.isBlank()) {
            errorMessage = "Please fill in all required fields"
            return
        }

        isSubmitting = true
        errorMessage = null

        coroutineScope.launch(Dispatchers.IO) {
            try {
                apiClient.client.patch("https://api.budgetingbud.com/api/budget/") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "id" to selectedBudgetId,
                            "name" to newName,
                            "total_amount" to amount
                        )
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to update budget. Please try again"
            } finally {
                isSubmitting = false
            }
        }
    }

    val budgetOptions = existingBudgetData.map { it.id.toString() to it.name }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (budgetOptions.isNotEmpty()) {
            DropdownSelector(
                label = "Select Budget",
                options = budgetOptions,
                selectedOption = selectedBudgetId?.toString() ?: "",
                onOptionSelected = { selectedIdString ->
                    selectedIdString.toIntOrNull()?.let { id ->
                        handleBudgetChange(id)
                    }
                }
            )
        }

        OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            leadingIcon = { Text("$") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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
