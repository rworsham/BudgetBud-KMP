package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.ui.components.AlertHandler
import com.budgetbud.kmp.ui.components.SuccessDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import io.ktor.client.request.*
import io.ktor.http.*


@Composable
fun BudgetGoalForm(
    budgetId: Int,
    modifier: Modifier = Modifier,
    apiClient: ApiClient,
    onSuccess: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun submitGoal() {
        if (amount.isBlank() || startDate == null || endDate == null) {
            errorMessage = "Please fill in all required fields"
            return
        }

        isSubmitting = true
        errorMessage = null

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val payload = mapOf(
                    "budget" to budgetId,
                    "target_balance" to amount,
                    "start_date" to startDate.toString(),
                    "end_date" to endDate.toString()
                )

                apiClient.client.post("https://api.budgetingbud.com/api/budget-goal/") {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }

                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to create new Budget Goal. Please try again"
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
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Goal Amount") },
            placeholder = { Text("e.g. 500") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Text("$", style = MaterialTheme.typography.titleMedium)
            }
        )

        FormDatePicker(
            label = "Start Date",
            selectedDate = startDate,
            onDateSelected = { startDate = it }
        )

        FormDatePicker(
            label = "End Date",
            selectedDate = endDate,
            onDateSelected = { endDate = it }
        )

        Button(
            onClick = { submitGoal() },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Submitting..." else "Submit")
        }

        if (showSuccessDialog) {
            SuccessDialog(onDismiss = { showSuccessDialog = false })
        }

        errorMessage?.let {
            AlertHandler(alertMessage = it)
        }
    }
}
