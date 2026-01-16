package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.CategoryHistoryData
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
    var transactions by remember { mutableStateOf<List<CategoryHistoryData>>(emptyList()) }
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
                            "category_id" to categoryId.toString()
                        )
                    )
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }

                val responseBody = response.bodyAsText()
                transactions = Json.decodeFromString<List<CategoryHistoryData>>(responseBody)

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
                .padding(16.dp)
        ) {
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    AlertHandler(alertMessage = errorMessage!!)
                }
                transactions.isEmpty() -> {
                    ChartDataError()
                }
                else -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(transactions, key = { it.id }) { tx ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                shadowElevation = 2.dp,
                                color = MaterialTheme.colorScheme.surface,
                                border = null
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tx.date,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                        )

                                        Text(
                                            text = if (tx.transaction_type == "income")
                                                "+$${tx.amount}"
                                            else
                                                "-$${tx.amount}",
                                            color = if (tx.transaction_type == "income")
                                                Color(0xFF1DB954)
                                            else
                                                MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text("Category", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            Text(tx.category, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Column(Modifier.weight(1f)) {
                                            Text("Budget", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            Text(tx.budget, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = tx.description.ifEmpty { "(No description)" },
                                        fontSize = 13.sp,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}