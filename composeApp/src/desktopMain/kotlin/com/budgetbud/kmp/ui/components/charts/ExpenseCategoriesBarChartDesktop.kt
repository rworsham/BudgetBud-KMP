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
import com.budgetbud.kmp.models.ExpenseCategoryBarChartData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.math.ceil

@Composable
actual fun ExpenseCategoriesBarChart(
    startDate: String,
    endDate: String,
    familyView: Boolean,
    modifier: Modifier,
    apiClient: ApiClient,
    onLoadingStatusChange: (isLoading: Boolean) -> Unit
) {
    var chartData by remember { mutableStateOf<List<ExpenseCategoryBarChartData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(startDate, endDate, familyView) {
        onLoadingStatusChange(true)
        try {
            val tokens = apiClient.getTokens()
            val response: HttpResponse = apiClient.client.post("https://api.budgetingbud.com/api/transaction-bar-chart/") {
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
                ExpenseCategoryBarChartData(
                    category = it["category"] ?: "Unknown",
                    total_amount = it["total_amount"]?.toFloatOrNull() ?: 0f
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
            text = "Expense Categories Bar Chart",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            error != null -> Text(error ?: "Unknown Error", color = MaterialTheme.colorScheme.error)
            chartData.isEmpty() -> Text("No data available", style = MaterialTheme.typography.bodyMedium)
            else -> DrawExpenseChart(chartData, Modifier.fillMaxWidth().height(350.dp))
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun DrawExpenseChart(data: List<ExpenseCategoryBarChartData>, modifier: Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val maxAmount = data.maxOfOrNull { it.total_amount } ?: 1f
    val roundedMax = (ceil(maxAmount / 1000f) * 1000f).coerceAtLeast(1000f)

    Canvas(
        modifier = modifier.padding(top = 16.dp, bottom = 64.dp, start = 80.dp, end = 16.dp)
    ) {
        val chartAreaWidth = size.width
        val chartAreaHeight = size.height
        val barWidth = (chartAreaWidth / data.size) * 0.6f
        val spaceBetween = chartAreaWidth / data.size
        val heightScale = chartAreaHeight / roundedMax

        val numberOfGridLines = 5
        for (i in 0..numberOfGridLines) {
            val y = i * chartAreaHeight / numberOfGridLines

            if (i < numberOfGridLines) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(chartAreaWidth, y),
                    strokeWidth = 1f
                )
            }

            val labelValue = roundedMax * (numberOfGridLines - i) / numberOfGridLines
            drawText(
                textMeasurer = textMeasurer,
                text = "$${String.format("%,.0f", labelValue)}",
                topLeft = Offset(-70.dp.toPx(), y - 10.dp.toPx()),
                style = TextStyle(
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        data.forEachIndexed { index, item ->
            val barHeight = item.total_amount * heightScale
            val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

            drawRect(
                color = Color(0xFF1DB954),
                topLeft = Offset(xOffset, chartAreaHeight - barHeight),
                size = Size(barWidth, barHeight)
            )

            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(item.category),
                style = TextStyle(color = Color.Gray, fontSize = 11.sp)
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = xOffset + (barWidth - textLayoutResult.size.width) / 2,
                    y = chartAreaHeight + 12.dp.toPx()
                )
            )
        }
    }
}