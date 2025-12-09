package com.budgetbud.kmp.ui.components.charts

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.BudgetReportData
import com.budgetbud.kmp.models.IncomeExpenseBarChartData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.math.ceil
import android.util.Log

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
                    tokens?.let {
                        append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                    }
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
            Log.e("IncExpBarChart", "Data fetch error", e)
        } finally {
            onLoadingStatusChange(false)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            error != null -> Text(error ?: "Error", color = Color.Red, modifier = Modifier.padding(16.dp))
            else -> DrawIncomeExpenseChart(incomeExpenseData, Modifier.fillMaxSize())
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun DrawIncomeExpenseChart(data: List<IncomeExpenseBarChartData>, modifier: Modifier) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
    val roundedMax = ceil(maxValue / 1000.0) * 1000.0

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(top = 16.dp, bottom = 48.dp, start = 64.dp, end = 16.dp)
        ) {
            val chartAreaWidth = size.width
            val chartAreaHeight = size.height

            val barWidthRatio = 0.6f
            val spacePerItem = chartAreaWidth / data.size
            val barWidth = spacePerItem * barWidthRatio

            val heightScale = chartAreaHeight / roundedMax.toFloat()

            val yAxisLabelXOffset = -70f

            val yAxisTextPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 24f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }

            val xAxisPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 24f
                textAlign = Paint.Align.CENTER
            }

            val numberOfGridLines = 5
            for (i in 0..numberOfGridLines) {
                val y = chartAreaHeight - (i * chartAreaHeight / numberOfGridLines)

                if (i > 0) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(chartAreaWidth, y),
                        strokeWidth = 1f
                    )
                }

                val value = roundedMax * (i / numberOfGridLines.toFloat())

                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%,.0f", value),
                    yAxisLabelXOffset,
                    y + yAxisTextPaint.descent() - 2f,
                    yAxisTextPaint
                )
            }

            data.forEachIndexed { index, item ->
                val barHeight = item.value * heightScale
                val xStart = index * spacePerItem + (spacePerItem - barWidth) / 2
                val xCenter = xStart + (barWidth / 2)

                drawRect(
                    color = if (item.name == "Income") Color(0xFF1DB954) else Color.Red,
                    topLeft = Offset(xStart, (chartAreaHeight - barHeight).toFloat()),
                    size = Size(barWidth, barHeight.toFloat())
                )

                drawContext.canvas.nativeCanvas.drawText(
                    item.name,
                    xCenter,
                    chartAreaHeight + 20f,
                    xAxisPaint
                )
            }
        }
    }
}