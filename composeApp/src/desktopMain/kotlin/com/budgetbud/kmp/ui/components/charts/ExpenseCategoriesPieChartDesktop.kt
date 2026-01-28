package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.ExpenseCategoriesPieChartData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

@Composable
actual fun ExpenseCategoriesPieChart(
    startDate: String,
    endDate: String,
    familyView: Boolean,
    modifier: Modifier,
    apiClient: ApiClient,
    onLoadingStatusChange: (isLoading: Boolean) -> Unit
) {
    var chartData by remember { mutableStateOf<List<ExpenseCategoriesPieChartData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(startDate, endDate, familyView) {
        onLoadingStatusChange(true)
        error = null
        try {
            val tokens = apiClient.getTokens()
            val response: HttpResponse = apiClient.client.post("https://api.budgetingbud.com/api/transaction-pie-chart/") {
                parameter("familyView", familyView)
                contentType(ContentType.Application.Json)
                setBody(mapOf("start_date" to startDate, "end_date" to endDate))
                headers {
                    tokens?.let {
                        append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                    }
                }
            }

            val rawData = response.body<List<Map<String, String>>>()
            chartData = rawData.map {
                ExpenseCategoriesPieChartData(
                    name = it["name"] ?: "Unknown",
                    value = it["value"] ?: "0"
                )
            }
        } catch (e: Exception) {
            error = "Failed to fetch data"
        } finally {
            onLoadingStatusChange(false)
        }
    }

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Expense Category Pie Chart",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when {
            error != null -> Text(error ?: "Unknown Error", color = MaterialTheme.colorScheme.error)
            chartData.isEmpty() -> Text("No data available", style = MaterialTheme.typography.bodyMedium)
            else -> DrawTransactionPieChart(chartData)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DrawTransactionPieChart(
    data: List<ExpenseCategoriesPieChartData>
) {
    val parsedData = remember(data) {
        data.map {
            val amount = it.value.toFloatOrNull() ?: 0f
            it to amount
        }.filter { it.second > 0f }
    }

    val totalAmount = parsedData.sumOf { it.second.toDouble() }.toFloat()

    val colors = listOf(
        Color(0xFF1DB954),
        Color(0xFF3E9E52),
        Color(0xFF70B37E),
        Color(0xFF9CCC99),
        Color(0xFFCCE5CC)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (totalAmount <= 0f) return@Column



        Canvas(
            modifier = Modifier
                .size(320.dp)
                .padding(16.dp)
        ) {
            var startAngle = -90f
            parsedData.forEachIndexed { index, (_, amount) ->
                val sweepAngle = (amount / totalAmount) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 4
        ) {
            parsedData.forEachIndexed { index, (item, amount) ->
                val percent = (amount / totalAmount) * 100
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colors[index % colors.size], shape = MaterialTheme.shapes.extraSmall)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.name}: ${"%.1f".format(percent)}%",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}