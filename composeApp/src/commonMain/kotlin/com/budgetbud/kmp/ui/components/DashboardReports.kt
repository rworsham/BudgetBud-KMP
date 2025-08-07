package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.Transaction
import com.budgetbud.kmp.ui.components.ChartDataError
import com.budgetbud.kmp.ui.components.DateRangeFilterForm
import com.budgetbud.kmp.ui.components.AlertHandler
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DashboardReports(
    familyView: Boolean,
    modifier: Modifier = Modifier,
    apiClient: ApiClient
) {
    val coroutineScope = rememberCoroutineScope()

    var startDate by remember { mutableStateOf(LocalDate(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.year, 1, 1)) }
    var endDate by remember { mutableStateOf(LocalDate(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.year, 12, 31)) }

    var barChartData by remember { mutableStateOf<List<BarChartData>>(emptyList()) }
    var pieChartData by remember { mutableStateOf<List<PieChartData>>(emptyList()) }
    var transactionRows by remember { mutableStateOf<List<Transaction>>(emptyList()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(startDate, endDate, familyView) {
        fetchData()
    }

    fun fetchData() {
        coroutineScope.launch {
            isLoading = true
            try {
                val params = mapOf("familyView" to familyView.toString())
                val datePayload = mapOf(
                    "start_date" to startDate.toString(),
                    "end_date" to endDate.toString()
                )

                val barResponse = apiClient.post<List<BarChartData>>("/transaction-bar-chart/", datePayload, params)
                val pieResponse = apiClient.post<List<PieChartData>>("/transaction-pie-chart/", datePayload, params)
                val tableResponse = apiClient.post<List<Transaction>>("/transaction-table-view/", datePayload, params)

                barChartData = barResponse
                pieChartData = pieResponse
                transactionRows = tableResponse
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        DateRangeFilterForm(
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = { startDate = it },
            onEndDateChange = { endDate = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Transaction Bar Chart", style = MaterialTheme.typography.titleMedium)
        if (barChartData.isNotEmpty()) {
            BarChartComponent(barChartData)
        } else {
            ChartDataError()
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Expense Pie Chart", style = MaterialTheme.typography.titleMedium)
        if (pieChartData.isNotEmpty()) {
            PieChartComponent(pieChartData)
        } else {
            ChartDataError()
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Transaction Grid", style = MaterialTheme.typography.titleMedium)
        if (transactionRows.isNotEmpty()) {
            TransactionTable(
                transactions = transactionRows,
                onDelete = { id ->
                    coroutineScope.launch {
                        try {
                            apiClient.delete("/transaction/$id")
                            transactionRows = transactionRows.filterNot { it.id == id }
                            showSuccessDialog = true
                        } catch (e: Exception) {
                            errorMessage = "Failed to delete transaction"
                        }
                    }
                },
                onUpdate = { updated ->
                    coroutineScope.launch {
                        try {
                            apiClient.put("/transaction/${updated.id}", updated)
                            transactionRows = transactionRows.map {
                                if (it.id == updated.id) updated else it
                            }
                            showSuccessDialog = true
                        } catch (e: Exception) {
                            errorMessage = "Failed to update transaction"
                        }
                    }
                }
            )
        } else {
            ChartDataError()
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("Close")
                }
            },
            title = { Text("Success") },
            text = { Text("Operation completed successfully.") }
        )
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    }

    errorMessage?.let {
        AlertHandler(alertMessage = it)
    }
}
