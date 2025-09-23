package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
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
actual fun FamilyHistory(
    userId: String,
    apiClient: ApiClient,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var startDate by remember { mutableStateOf(DateUtils.firstDayOfCurrentMonth()) }
    var endDate by remember { mutableStateOf(DateUtils.lastDayOfCurrentMonth()) }
    var transactions by remember { mutableStateOf<List<TransactionHistoryTableData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun fetchTransactions(downloadPdf: Boolean = false) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val response: HttpResponse = apiClient.client.post("https://api.budgetingbud.com/api/family/history/") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        buildMap {
                            put("start_date", startDate.toString())
                            put("end_date", endDate.toString())
                            put("user_id", userId.toInt())
                            if (downloadPdf) put("format", "pdf")
                        }
                    )
                }

                if (downloadPdf) {
                    //Wip
                } else {
                    val responseBody = response.bodyAsText()
                    transactions = Json.decodeFromString(responseBody)
                }

            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch data"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(startDate, endDate, userId) {
        fetchTransactions()
    }

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        DateRangeFilterForm(
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = { startDate = it },
            onEndDateChange = { endDate = it },
            onSubmit = { fetchTransactions() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> CircularProgressIndicator()
            errorMessage != null -> AlertHandler(alertMessage = errorMessage!!)
            transactions.isEmpty() -> ChartDataError()
            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Text("ID", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("Date", Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                    Text("Amount", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("Type", Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                    Text("Description", Modifier.weight(3f), style = MaterialTheme.typography.bodyMedium)
                    Text("Category", Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                    Text("Budget", Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                    Text("Account", Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                }

                Divider()

                LazyColumn {
                    items(transactions) { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(tx.id.toString(), Modifier.weight(1f))
                            Text(tx.date, Modifier.weight(2f))
                            Text("$${tx.amount}", Modifier.weight(1f))
                            Text(tx.transaction_type, Modifier.weight(2f))
                            Text(tx.description, Modifier.weight(3f))
                            Text(tx.category, Modifier.weight(2f))
                            Text(tx.budget, Modifier.weight(2f))
                            Text(tx.account, Modifier.weight(2f))
                        }
                        Divider()
                    }
                }
            }
        }
    }
}
