package com.budgetbud.kmp.ui.components.charts

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
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

    when {
        error != null -> Text(error ?: "", color = Color.Red)
        else -> DrawExpenseChart(chartData, modifier)
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun DrawExpenseChart(data: List<ExpenseCategoryBarChartData>, modifier: Modifier) {
    val maxAmount = data.maxOfOrNull { it.total_amount } ?: 1f
    val roundedMax = ceil(maxAmount / 1000f) * 1000f

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(top = 16.dp, bottom = 48.dp, start = 64.dp, end = 16.dp)
        ) {
            val barWidth = size.width / data.size * 0.6f
            val spaceBetween = size.width / data.size
            val heightScale = if (roundedMax == 0f) 0f else size.height / roundedMax

            val yAxisTextPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 24f
                textAlign = Paint.Align.RIGHT
                typeface = Typeface.DEFAULT_BOLD
            }

            val numberOfGridLines = 5
            for (i in 0..numberOfGridLines) {
                val y = i * size.height / numberOfGridLines

                if (i < numberOfGridLines) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                val labelValue = roundedMax * (numberOfGridLines - i) / numberOfGridLines.toFloat()

                drawContext.canvas.nativeCanvas.drawText(
                    "$${String.format("%.0f", labelValue)}",
                    -10f,
                    y + yAxisTextPaint.textSize / 3,
                    yAxisTextPaint
                )
            }

            val xAxisPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 24f
                textAlign = Paint.Align.CENTER
            }

            data.forEachIndexed { index, item ->
                val barHeight = item.total_amount * heightScale
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = Color(0xFF1DB954),
                    topLeft = Offset(xOffset, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    item.category,
                    xOffset + (barWidth / 2),
                    size.height + 24f,
                    xAxisPaint
                )
            }
        }
    }
}