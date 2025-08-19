package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.AccountHistoryData
import com.budgetbud.kmp.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
actual fun AccountHistory(
    accountId: Int,
    apiClient: ApiClient,
    familyView: Boolean,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var transactions by remember { mutableStateOf<List<AccountHistoryData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var startDate by remember { mutableStateOf(DateUtils.firstDayOfCurrentMonth()) }
    var endDate by remember { mutableStateOf(DateUtils.lastDayOfCurrentMonth()) }

    fun fetchTransactions() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                transactions = fetchAccountHistory(apiClient, accountId, startDate.toString(), endDate.toString(), familyView)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load account history"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(accountId, startDate, endDate, familyView) {
        fetchTransactions()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
       // DatePicker

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
        } else if (transactions.isEmpty()) {
            Text("No transactions available")
        } else {
            transactions.forEach { tx ->
                Text("ID: ${tx.id} | Date: ${tx.date} | Amount: $${tx.amount}")
                Text("Desc: ${tx.description} | Type: ${tx.transaction_type}")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
