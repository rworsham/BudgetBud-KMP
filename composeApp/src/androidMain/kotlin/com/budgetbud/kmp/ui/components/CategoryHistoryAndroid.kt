package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.TransactionHistoryTableData
import com.budgetbud.kmp.ui.components.forms.DateRangeFilterForm
import com.budgetbud.kmp.utils.DateUtils
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
actual fun CategoryHistory(
    apiClient: ApiClient,
    categoryId: Int,
    familyView: Boolean,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var startDate by remember { mutableStateOf(DateUtils.firstDayOfCurrentMonth()) }
    var endDate by remember { mutableStateOf(DateUtils.lastDayOfCurrentMonth()) }
    var transactions by remember { mutableStateOf<List<TransactionHistoryTableData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun fetchTransactions() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val tokens = apiClient.getTokens()
                val response: HttpResponse = apiClient.client.post("https://api.budgetingbud.com/api/category/history/") {
                    parameter("familyView", familyView)
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "start_date" to startDate.toString(),
                            "end_date" to endDate.toString(),
                            "category_id" to categoryId
                        )
                    )
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }

                val responseBody = response.bodyAsText()
                transactions = Json.decodeFromString<List<TransactionHistoryTableData>>(responseBody)

            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch transactions"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(startDate, endDate, categoryId, familyView) {
        fetchTransactions()
    }

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        DateRangeFilterForm(
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = { newStartDate ->
                startDate = newStartDate
                fetchTransactions()
            },
            onEndDateChange = { newEndDate ->
                endDate = newEndDate
                fetchTransactions()
            },
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            errorMessage != null -> {
                AlertHandler(alertMessage = errorMessage!!)
            }
            transactions.isEmpty() -> {
                ChartDataError()
            }
            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Text("ID", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("Date", modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                    Text("Amount", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("Type", modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                    Text("Description", modifier = Modifier.weight(3f), style = MaterialTheme.typography.bodyMedium)
                    Text("Category", modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                    Text("Budget", modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                    Text("Account", modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                    Text("Recurring", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                }

                Divider()

                LazyColumn {
                    items(transactions) { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(tx.id.toString(), modifier = Modifier.weight(1f))
                            Text(tx.date, modifier = Modifier.weight(2f))
                            Text("$${tx.amount}", modifier = Modifier.weight(1f))
                            Text(tx.transaction_type, modifier = Modifier.weight(2f))
                            Text(tx.description, modifier = Modifier.weight(3f))
                            Text(tx.category, modifier = Modifier.weight(2f))
                            Text(tx.budget, modifier = Modifier.weight(2f))
                            Text(tx.account, modifier = Modifier.weight(2f))
                            Text(
                                if (tx.is_recurring) "Yes" else "No",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
