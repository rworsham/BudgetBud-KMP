package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    userId: Int,
    apiClient: ApiClient,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val horizontalScrollState = rememberScrollState()

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
                val tokens = apiClient.getTokens()
                val response: HttpResponse = apiClient.client.post("https://api.budgetingbud.com/api/family/history/") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        buildMap {
                            put("start_date", startDate.toString())
                            put("end_date", endDate.toString())
                            put("user_id", userId)
                            if (downloadPdf) put("format", "pdf")
                        }
                    )
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }

                if (!downloadPdf) {
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

    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            DateRangeFilterForm(
                startDate = startDate,
                endDate = endDate,
                onStartDateChange = { startDate = it; fetchTransactions() },
                onEndDateChange = { endDate = it; fetchTransactions() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                errorMessage != null -> AlertHandler(alertMessage = errorMessage!!)
                transactions.isEmpty() -> ChartDataError()
                else -> {
                    Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                        val rowModifier = Modifier.widthIn(min = 1000.dp).fillMaxWidth()

                        // Header Row
                        Surface(
                            modifier = rowModifier,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
                                Text("ID", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                                Text("Date", Modifier.weight(2f), style = MaterialTheme.typography.labelLarge)
                                Text("Amount", Modifier.weight(1.5f), style = MaterialTheme.typography.labelLarge)
                                Text("Type", Modifier.weight(2f), style = MaterialTheme.typography.labelLarge)
                                Text("Description", Modifier.weight(3f), style = MaterialTheme.typography.labelLarge)
                                Text("Category", Modifier.weight(2f), style = MaterialTheme.typography.labelLarge)
                                Text("Budget", Modifier.weight(2f), style = MaterialTheme.typography.labelLarge)
                                Text("Account", Modifier.weight(2f), style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        HorizontalDivider()

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(transactions) { tx ->
                                Row(
                                    modifier = rowModifier
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tx.id.toString(), Modifier.weight(1f))
                                    Text(tx.date, Modifier.weight(2f))
                                    Text("$${tx.amount}", Modifier.weight(1.5f), color = MaterialTheme.colorScheme.primary)
                                    Text(tx.transaction_type, Modifier.weight(2f))
                                    Text(tx.description, Modifier.weight(3f), maxLines = 1)
                                    Text(tx.category, Modifier.weight(2f))
                                    Text(tx.budget, Modifier.weight(2f))
                                    Text(tx.account, Modifier.weight(2f))
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}