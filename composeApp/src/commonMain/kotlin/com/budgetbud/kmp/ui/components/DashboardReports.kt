package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.TransactionBarChartData
import com.budgetbud.kmp.models.TransactionPieChartData
import com.budgetbud.kmp.models.TransactionTableData
import com.budgetbud.kmp.ui.components.charts.TransactionBarChart
import com.budgetbud.kmp.ui.components.charts.TransactionPieChart
import com.budgetbud.kmp.ui.components.forms.DateRangeFilterForm
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.call.body
import kotlinx.coroutines.launch
import com.budgetbud.kmp.utils.DateUtils


@Composable
fun DashboardReports(
    familyView: Boolean,
    modifier: Modifier = Modifier,
    apiClient: ApiClient,
) {
    val coroutineScope = rememberCoroutineScope()

    var startDate by remember { mutableStateOf(DateUtils.firstDayOfCurrentMonth()) }
    var endDate by remember { mutableStateOf(DateUtils.lastDayOfCurrentMonth()) }

    var barChartData by remember { mutableStateOf<List<TransactionBarChartData>>(emptyList()) }
    var pieChartData by remember { mutableStateOf<List<TransactionPieChartData>>(emptyList()) }
    var transactionRows by remember { mutableStateOf<List<TransactionTableData>>(emptyList()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    fun fetchData() {
        coroutineScope.launch {
            isLoading = true
            try {
                val tokens = apiClient.getTokens()
                val queryParams = listOf("familyView" to familyView.toString())
                val datePayload = mapOf(
                    "start_date" to startDate.toString(),
                    "end_date" to endDate.toString()
                )

                fun HttpRequestBuilder.attachAuthAndQueryParams() {
                    contentType(ContentType.Application.Json)
                    setBody(datePayload)
                    url {
                        queryParams.forEach { (key, value) ->
                            parameters.append(key, value)
                        }
                    }
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }

                val barResponse = apiClient.client.post("https://api.budgetingbud.com/api/transaction-bar-chart/") {
                    attachAuthAndQueryParams()
                }.body<List<TransactionBarChartData>>()

                val pieResponse = apiClient.client.post("https://api.budgetingbud.com/api/transaction-pie-chart/") {
                    attachAuthAndQueryParams()
                }.body<List<TransactionPieChartData>>()

                val tableResponse = apiClient.client.post("https://api.budgetingbud.com/api/transaction-table-view/") {
                    attachAuthAndQueryParams()
                }.body<List<TransactionTableData>>()

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

    LaunchedEffect(startDate, endDate, familyView) {
        fetchData()
    }

    Column(modifier = modifier.padding(16.dp)) {
        DateRangeFilterForm(
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = { startDate = it },
            onEndDateChange = { endDate = it },
            onSubmit = {
                fetchData()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Transaction Bar Chart", style = MaterialTheme.typography.titleMedium)
        if (barChartData.isNotEmpty()) {
            TransactionBarChart(barChartData)
        } else {
            ChartDataError()
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Expense Pie Chart", style = MaterialTheme.typography.titleMedium)
        if (pieChartData.isNotEmpty()) {
            TransactionPieChart(pieChartData)
        } else {
            ChartDataError()
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Transaction Grid", style = MaterialTheme.typography.titleMedium)
        if (transactionRows.isNotEmpty()) {
            TransactionTable(
                familyView = familyView,
                apiClient = apiClient
            )
        } else {
            ChartDataError()
        }
    }

    if (showSuccessDialog) {
        SuccessDialog(
            onDismiss = { showSuccessDialog = false }
        )
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    }

    errorMessage?.let {
        AlertHandler(alertMessage = it)
    }
}
