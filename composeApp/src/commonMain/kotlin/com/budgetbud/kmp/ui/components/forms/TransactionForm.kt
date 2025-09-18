package com.budgetbud.kmp.ui.components.forms

import com.budgetbud.kmp.models.CategoryData
import com.budgetbud.kmp.models.BudgetData
import com.budgetbud.kmp.models.AccountData
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.ui.components.AlertHandler
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
fun TransactionForm(
    apiClient: ApiClient,
    onSuccess: () -> Unit,
    familyView: Boolean = false
) {
    var date by remember { mutableStateOf<LocalDate?>(null) }
    var amount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("expense") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringType by remember { mutableStateOf("") }
    var nextOccurrence by remember { mutableStateOf("") }

    var categories by remember { mutableStateOf<List<CategoryData>>(emptyList()) }
    var budgetData by remember { mutableStateOf<List<BudgetData>>(emptyList()) }
    var accounts by remember { mutableStateOf<List<AccountData>>(emptyList()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val catsResponse: HttpResponse = apiClient.client.get("https://api.budgetingbud.com/api/categories/") {
                parameter("familyView", familyView)
            }
            val budgetsResponse: HttpResponse = apiClient.client.get("https://api.budgetingbud.com/api/budget/") {
                parameter("familyView", familyView)
            }
            val accountsResponse: HttpResponse = apiClient.client.get("https://api.budgetingbud.com/api/accounts/") {
                parameter("familyView", familyView)
            }

            categories = catsResponse.body()
            budgetData = budgetsResponse.body()
            accounts = accountsResponse.body()
        } catch (e: Exception) {
            errorMessage = "Failed to fetch data"
        } finally {
            isLoading = false
        }
    }

    fun validateForm(): Boolean {
        if (date.isBlank() || amount.isBlank() || category.isBlank() || budget.isBlank() || account.isBlank()) {
            errorMessage = "Please fill in all required fields."
            return false
        }
        if (isRecurring && recurringType.isBlank()) {
            errorMessage = "Please select a recurring type."
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
                apiClient.client.post("https://api.budgetingbud.com/api/transaction/") {
                    contentType(ContentType.Application.Json)
                    parameter("familyView", familyView)
                    setBody(
                        mapOf(
                            "date" to date,
                            "amount" to amount,
                            "transaction_type" to transactionType,
                            "description" to description,
                            "category" to category,
                            "budget" to budget,
                            "account" to account,
                            "is_recurring" to isRecurring,
                            "recurring_type" to recurringType,
                            "next_occurrence" to nextOccurrence
                        )
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to create transaction. Please try again."
            } finally {
                isSubmitting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FormDatePicker(
            label = "Date",
            selectedDate = date,
            onDateSelected = { date = it },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        DropdownSelector(
            label = "Transaction Type",
            options = listOf("income" to "Income", "expense" to "Expense"),
            selectedOption = transactionType,
            onOptionSelected = { transactionType = it }
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        DropdownSelector(
            label = "Category",
            options = categories.map { it.id to it.name },
            selectedOption = category,
            onOptionSelected = { category = it }
        )

        DropdownSelector(
            label = "Account",
            options = accounts.map { it.id to it.name },
            selectedOption = account,
            onOptionSelected = { account = it }
        )

        DropdownSelector(
            label = "Budget",
            options = budgetData.map { it.id to it.name },
            selectedOption = budget,
            onOptionSelected = { budget = it }
        )

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(
                checked = isRecurring,
                onCheckedChange = { isRecurring = it }
            )
            Text("Is Recurring")
        }

        if (isRecurring) {
            DropdownSelector(
                label = "Recurring Type",
                options = listOf(
                    "daily" to "Daily",
                    "weekly" to "Weekly",
                    "monthly" to "Monthly",
                    "yearly" to "Yearly"
                ),
                selectedOption = recurringType,
                onOptionSelected = { recurringType = it }
            )

            OutlinedTextField(
                value = nextOccurrence,
                onValueChange = { nextOccurrence = it },
                label = { Text("Next Occurrence (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

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
