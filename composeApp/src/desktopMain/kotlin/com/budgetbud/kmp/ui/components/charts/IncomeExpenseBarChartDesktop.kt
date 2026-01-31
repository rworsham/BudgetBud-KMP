package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.BudgetReportData
import com.budgetbud.kmp.models.IncomeExpenseBarChartData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.math.ceil

@Composable
actual fun IncomeExpenseBarChart(
    startDate: String,
    endDate: String,
    familyView: Boolean,
    modifier: Modifier,
    apiClient: ApiClient,
    onLoadingStatusChange: (isLoading: Boolean) -> Unit,
) {
    var incomeExpenseData by remember { mutableStateOf<List<IncomeExpenseBarChartData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(startDate, endDate, familyView) {
        onLoadingStatusChange(true)
        try {
            val tokens = apiClient.getTokens()
            val response: HttpResponse = apiClient.client.post("https://api.budgetingbud.com/api/budget-transaction-overview/") {
                parameter("familyView", familyView)
                contentType(ContentType.Application.Json)
                setBody(mapOf("start_date" to startDate, "end_date" to endDate))
                headers {
                    tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") }
                }
            }

            val reportData = response.body<BudgetReportData>()
            val rawData = reportData.transactions

            incomeExpenseData = listOf(
                IncomeExpenseBarChartData(
                    name = "Income",
                    value = rawData.filter { it.transaction_type == "income" }
                        .sumOf { (it.amount.toFloatOrNull() ?: 0f).toDouble() }
                ),
                IncomeExpenseBarChartData(
                    name = "Expense",
                    value = rawData.filter { it.transaction_type == "expense" }
                        .sumOf { (it.amount.toFloatOrNull() ?: 0f).toDouble() }
                )
            )
        } catch (e: Exception) {
            error = "Failed to fetch data: ${e.message}"
            println("IncExpBarChart: Data fetch error: ${e.message}")
        } finally {
            onLoadingStatusChange(false)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            error != null -> Text(error!!, color = Color.Red, modifier = Modifier.padding(16.dp))
            else -> DrawIncomeExpenseChart(incomeExpenseData, Modifier.fillMaxSize())
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun DrawIncomeExpenseChart(data: List<IncomeExpenseBarChartData>, modifier: Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
    val roundedMax = (ceil(maxValue / 1000.0) * 1000.0).coerceAtLeast(1000.0)

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Income Vs Expense Bar Chart",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(top = 16.dp, bottom = 64.dp, start = 80.dp, end = 32.dp)
        ) {
            val chartAreaWidth = size.width
            val chartAreaHeight = size.height
            val spacePerItem = chartAreaWidth / data.size
            val barWidth = spacePerItem * 0.5f
            val heightScale = chartAreaHeight / roundedMax.toFloat()

            val numberOfGridLines = 5
            for (i in 0..numberOfGridLines) {
                val y = chartAreaHeight - (i * chartAreaHeight / numberOfGridLines)

                if (i > 0) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(chartAreaWidth, y),
                        strokeWidth = 1f
                    )
                }

                val labelValue = roundedMax * (i / numberOfGridLines.toDouble())
                drawText(
                    textMeasurer = textMeasurer,
                    text = String.format("%,.0f", labelValue),
                    topLeft = Offset(-70.dp.toPx(), y - 10.dp.toPx()),
                    style = TextStyle(
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            data.forEachIndexed { index, item ->
                val barHeight = (item.value * heightScale).toFloat()
                val xStart = index * spacePerItem + (spacePerItem - barWidth) / 2

                drawRect(
                    color = if (item.name == "Income") Color(0xFF1DB954) else Color(0xFFE53935),
                    topLeft = Offset(xStart, chartAreaHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )

                val labelResult = textMeasurer.measure(
                    text = AnnotatedString(item.name),
                    style = TextStyle(color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                )

                drawText(
                    textLayoutResult = labelResult,
                    topLeft = Offset(
                        x = xStart + (barWidth - labelResult.size.width) / 2,
                        y = chartAreaHeight + 12.dp.toPx()
                    )
                )
            }
        }
    }
}