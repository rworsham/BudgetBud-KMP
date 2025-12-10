package com.budgetbud.kmp.ui.components.charts

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
import androidx.compose.ui.text.font.FontWeight
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
    modifier: Modifier,
    onLoadingStatusChange: (isLoading: Boolean) -> Unit,
) {
    var data by remember { mutableStateOf<List<FamilyTransactionOverviewData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(familyView) {
        onLoadingStatusChange(true)
        error = null
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

            data = response.body<List<FamilyTransactionOverviewData>>()

        } catch (e: Exception) {
            error = "Failed to fetch contribution data: ${e.message}"
        } finally {
            onLoadingStatusChange(false)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            error != null -> Text(error ?: "Error", color = Color.Red, modifier = Modifier.padding(16.dp))
            data.isEmpty() -> Text("No data available", modifier = Modifier.padding(16.dp))
            else -> FamilyContributionChart(data, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun FamilyContributionChart(data: List<FamilyTransactionOverviewData>, modifier: Modifier) {
    val maxCount = data.maxOfOrNull { it.transaction_count } ?: 1f
    val roundedMax = ceil(maxCount / 10f) * 10f

    Column(modifier = modifier.fillMaxWidth()) {

        Text(
            text = "Family Contributions Bar Chart",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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

            val heightScale = chartAreaHeight / roundedMax

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
                val barHeight = item.transaction_count * heightScale
                val xStart = index * spacePerItem + (spacePerItem - barWidth) / 2
                val xCenter = xStart + (barWidth / 2)

                drawRect(
                    color = Color(0xFF1DB954),
                    topLeft = Offset(xStart, chartAreaHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    item.name,
                    xCenter,
                    chartAreaHeight + 24f,
                    xAxisPaint
                )
            }
        }
    }
}