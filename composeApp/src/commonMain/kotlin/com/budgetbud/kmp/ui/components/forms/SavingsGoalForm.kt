package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.ui.components.AlertHandler
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import com.budgetbud.kmp.ui.components.DatePickerField

@Composable
fun SavingsGoalForm(
    apiClient: ApiClient,
    accountId: String,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    fun submitForm() {
        if (amount.isBlank() || startDate == null || endDate == null) {
            error = "Please fill in all required fields"
            return
        }

        isSubmitting = true
        error = null

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE
                val response = apiClient.client.post("https://api.budgetingbud.com/api/account/savings-goal/") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "account" to accountId,
                            "target_balance" to amount,
                            "start_date" to formatter.format(startDate!!.toJavaLocalDate()),
                            "end_date" to formatter.format(endDate!!.toJavaLocalDate())
                        )
                    )
                }

                response.body<Unit>()
                onSuccess()

            } catch (e: Exception) {
                error = "Failed to create new Savings Goal. Please try again"
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
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        DatePickerField(
            label = "Start Date",
            selectedDate = startDate,
            onDateSelected = { startDate = it }
        )

        DatePickerField(
            label = "End Date",
            selectedDate = endDate,
            onDateSelected = { endDate = it }
        )

        Button(
            onClick = { submitForm() },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Submitting..." else "Submit")
        }

        error?.let {
            AlertHandler(alertMessage = it)
        }
    }
}