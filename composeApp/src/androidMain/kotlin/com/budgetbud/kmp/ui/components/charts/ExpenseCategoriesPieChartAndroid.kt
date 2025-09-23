package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    xSizePercent: Int,
    ySizePercent: Int,
    familyView: Boolean,
    modifier: Modifier,
    apiClient: ApiClient
) {
    var chartData by remember { mutableStateOf<List< ExpenseCategoriesPieChartData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(startDate, endDate, familyView) {
        isLoading = true
        error = null

        try {
            val response: HttpResponse = apiClient.client.post("https://api.budgetingbud.com/api/transaction-pie-chart/") {
                parameter("familyView", familyView)
                contentType(ContentType.Application.Json)
                setBody(mapOf("start_date" to startDate, "end_date" to endDate))
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
            isLoading = false
        }
    }

    when {
        isLoading -> CircularProgressIndicator()
        error != null -> Text(error ?: "", color = Color.Red)
        chartData.isEmpty() -> Text("No data available")
        else -> DrawTransactionPieChart(chartData, modifier)
    }
}

@Composable
private fun DrawTransactionPieChart(
    data: List<ExpenseCategoriesPieChartData>,
    modifier: Modifier = Modifier
) {
    val parsedData = data.map {
        val amount = it.value.toFloatOrNull() ?: 0f
        it to amount
    }.filter { it.second > 0f }

    val totalAmount = parsedData.sumOf { it.second.toDouble() }.toFloat().takeIf { it > 0f } ?: return

    val colors = listOf(
        Color(0xFF1DB954),
        Color(0xFF3E9E52),
        Color(0xFF70B37E),
        Color(0xFF9CCC99),
        Color(0xFFCCE5CC)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .size(250.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            parsedData.forEachIndexed { index, (item, amount) ->
                val percent = (amount / totalAmount) * 100
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colors[index % colors.size])
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.name}: ${"%.1f".format(percent)}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
