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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.FamilyCategoryOverviewData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.math.ceil

@Composable
actual fun CategoryUsagePerUserBarChart(
    startDate: String,
    endDate: String,
    familyView: Boolean,
    apiClient: ApiClient,
    modifier: Modifier,
    onLoadingStatusChange: (isLoading: Boolean) -> Unit
) {
    var data by remember { mutableStateOf<List<FamilyCategoryOverviewData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(startDate, endDate, familyView) {
        onLoadingStatusChange(true)
        error = null
        try {
            val tokens = apiClient.getTokens()

            val datePayload = mapOf(
                "start_date" to startDate,
                "end_date" to endDate
            )

            val response: HttpResponse = apiClient.client.post("https://api.budgetingbud.com/api/family/overview/") {
                contentType(ContentType.Application.Json)
                setBody(datePayload)
                url {
                    parameters.append("Category", "true")
                    parameters.append("familyView", familyView.toString())
                }
                headers {
                    tokens?.let {
                        append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                    }
                }
            }

            data = response.body<List<FamilyCategoryOverviewData>>()

        } catch (e: Exception) {
            error = "Failed to fetch chart data: ${e.message}"
        } finally {
            onLoadingStatusChange(false)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        when {
            error != null -> Text(error ?: "Error", color = Color.Red, modifier = Modifier.padding(16.dp))
            data.isEmpty() -> Text("No data available for this period.", modifier = Modifier.padding(16.dp))
            else -> FamilyBarChart(data)
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun FamilyBarChart(data: List<FamilyCategoryOverviewData>) {
    val maxCount = data.maxOfOrNull { it.category_count } ?: 1f
    val roundedMax = ceil(maxCount / 10f) * 10f

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = "Family Category Usage Bar Chart",
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
            val barWidth = (chartAreaWidth / data.size * 0.6f)
            val spaceBetween = (chartAreaWidth / data.size)
            val heightScale = chartAreaHeight / roundedMax

            val yAxisTextPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 24f
                textAlign = Paint.Align.RIGHT
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
                    -8.dp.toPx(),
                    y + yAxisTextPaint.descent() - 2f,
                    yAxisTextPaint
                )
            }

            data.forEachIndexed { index, item ->
                val barHeight = item.category_count * heightScale
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = Color(0xFF1DB954),
                    topLeft = Offset(xOffset, chartAreaHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    item.name,
                    xOffset + (barWidth / 2),
                    chartAreaHeight + 20f,
                    xAxisPaint
                )
            }
        }
    }
}