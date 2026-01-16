package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.AccountHistoryTableData
import com.budgetbud.kmp.ui.components.forms.DateRangeFilterForm
import com.budgetbud.kmp.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
actual fun BudgetHistory(
    budgetId: Int,
    apiClient: ApiClient,
    familyView: Boolean,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var transactions by remember { mutableStateOf<List<AccountHistoryTableData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var startDate by remember { mutableStateOf(DateUtils.firstDayOfCurrentMonth()) }
    var endDate by remember { mutableStateOf(DateUtils.lastDayOfCurrentMonth()) }

    fun fetchTransactions() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                transactions = fetchBudgetHistory(apiClient, budgetId, startDate.toString(), endDate.toString(), familyView)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load budget history"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(budgetId, startDate, endDate, familyView) {
        fetchTransactions()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            DateRangeFilterForm(
                startDate = startDate,
                endDate = endDate,
                onStartDateChange = { startDate = it },
                onEndDateChange = { endDate = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            } else if (transactions.isEmpty()) {
                Text("No transactions available", style = MaterialTheme.typography.bodyLarge)
            } else {
                transactions.forEach { tx ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "ID: ${tx.id} | Date: ${tx.date} | Amount: $${tx.amount}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Desc: ${tx.description} | Type: ${tx.transaction_type}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}