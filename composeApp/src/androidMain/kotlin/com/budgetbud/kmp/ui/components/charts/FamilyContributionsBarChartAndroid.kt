package com.budgetbud.kmp.ui.components.charts

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
import com.budgetbud.kmp.models.FamilyTransactionOverviewData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.math.ceil

@Composable
actual fun FamilyContributionsBarChart(
    startDate: String,
    endDate: String,
    familyView: Boolean,
    apiClient: ApiClient,
    modifier: Modifier
) {
    var data by remember { mutableStateOf<List<FamilyTransactionOverviewData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(familyView) {
        isLoading = true
        try {
            val tokens = apiClient.getTokens()
            val response: HttpResponse = apiClient.client.get("https://api.budgetingbud.com/api/family/overview/") {
                parameter("Transaction", true)
                contentType(ContentType.Application.Json)
                headers {
                    tokens?.let {
                        append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                    }
                }
            }

            val rawData = response.body<List<Map<String, Any>>>()

            data = rawData.map {
                FamilyTransactionOverviewData(
                    name = it["name"]?.toString() ?: "Unknown",
                    transaction_count = (it["transaction_count"] as? Number)?.toFloat() ?: 0f
                )
            }
        } catch (e: Exception) {
            error = "Failed to fetch account data"
        } finally {
            isLoading = false
        }
    }

    Box(

    ) {
        when {
            isLoading -> CircularProgressIndicator()
            error != null -> Text(error ?: "Error", color = Color.Red)
            data.isEmpty() -> Text("No data available")
            else -> FamilyContributionChart(data)
        }
    }
}

@Composable
private fun FamilyContributionChart(data: List<FamilyTransactionOverviewData>) {
    val maxCount = data.maxOfOrNull { it.transaction_count } ?: 1f
    val roundedMax = ceil(maxCount / 10f) * 10f

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 4.dp)
        ) {
            val barWidth = size.width / data.size * 0.6f
            val spaceBetween = size.width / data.size
            val heightScale = size.height / roundedMax
            val labelXOffset = 4.dp.toPx()

            val yAxisTextPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 20f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }

            val xAxisPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 20f
                textAlign = Paint.Align.CENTER
            }

            val numberOfGridLines = 5
            for (i in 1 until numberOfGridLines) {
                val y = size.height - (i * size.height / numberOfGridLines)

                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )

                val value = roundedMax * (i / numberOfGridLines.toFloat())

                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%,.0f", value),
                    labelXOffset,
                    y - yAxisTextPaint.descent(),
                    yAxisTextPaint
                )
            }

            for (i in 1 until data.size) {
                val x = (i * spaceBetween)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            data.forEachIndexed { index, item ->
                val barHeight = item.transaction_count * heightScale
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = Color(0xFF1DB954),
                    topLeft = Offset(xOffset, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    item.name,
                    xOffset + (barWidth / 2),
                    size.height + 24f,
                    xAxisPaint
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}