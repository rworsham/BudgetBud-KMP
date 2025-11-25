package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.IncomeExpenseBarChartData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.math.ceil

@Composable
actual fun IncomeExpenseBarChart(
    startDate: String,
    endDate: String,
    familyView: Boolean,
    modifier: Modifier,
    apiClient: ApiClient
) {
    var incomeExpenseData by remember { mutableStateOf<List<IncomeExpenseBarChartData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(startDate, endDate, familyView) {
        isLoading = true
        try {
            val tokens = apiClient.getTokens()
            val response = apiClient.client.post("https://api.budgetingbud.com/api/budget-transaction-overview/") {
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


            incomeExpenseData = listOf(
                IncomeExpenseBarChartData(
                    name = "Income",
                    value = rawData.filter { it["transaction_type"] == "income" }
                        .sumOf { (it["amount"]?.toFloatOrNull() ?: 0f).toDouble() }
                ),
                IncomeExpenseBarChartData(
                    name = "Expense",
                    value = rawData.filter { it["transaction_type"] == "expense" }
                        .sumOf { (it["amount"]?.toFloatOrNull() ?: 0f).toDouble() }
                )
            )
        } catch (e: Exception) {
            error = "Failed to fetch data"
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> CircularProgressIndicator(modifier = modifier)
        error != null -> Text(error ?: "", color = Color.Red, modifier = modifier)
        incomeExpenseData.isEmpty() -> Text("No data available", modifier = modifier)
        else -> DrawIncomeExpenseChart(incomeExpenseData, modifier)
    }
}

@Composable
private fun DrawIncomeExpenseChart(data: List<IncomeExpenseBarChartData>, modifier: Modifier) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
    val roundedMaxValue = ceil(maxValue / 1000.0) * 1000.0

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 16.dp)
        ) {
            val barWidth = (size.width / data.size * 0.6f).toDouble()
            val spaceBetween = (size.width / data.size).toDouble()
            val heightScale = size.height / roundedMaxValue

            data.forEachIndexed { index, item ->
                val barHeight = item.value * heightScale
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = if (item.name == "Income") Color.Green else Color.Red,
                    topLeft = Offset(xOffset.toFloat(), (size.height - barHeight).toFloat()),
                    size = Size(barWidth.toFloat(), barHeight.toFloat())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            data.forEach {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
        }
    }
}
