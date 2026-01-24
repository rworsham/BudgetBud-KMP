package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.budgetbud.kmp.models.BudgetRemainingBudgetBarChartData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.math.ceil

@Composable
actual fun BudgetRemainingBudgetBarChart(
    startDate: String,
    endDate: String,
    familyView: Boolean,
    modifier: Modifier,
    apiClient: ApiClient,
    onLoadingStatusChange: (isLoading: Boolean) -> Unit
) {
    var chartItems by remember { mutableStateOf<List<BudgetRemainingBudgetBarChartData>>(emptyList()) }
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

            val reportData = response.body<com.budgetbud.kmp.models.BudgetReportData>()
            chartItems = reportData.budgets_remaining.map {
                BudgetRemainingBudgetBarChartData(
                    name = it.budget_name,
                    starting_budget = it.starting_budget.toDoubleOrNull() ?: 0.0,
                    remaining_budget = it.remaining_budget.toDoubleOrNull() ?: 0.0
                )
            }
        } catch (e: Exception) {
            error = "Error fetching chart data: ${e.message}"
        } finally {
            onLoadingStatusChange(false)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Budget Vs Remaining Budget",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        } else {
            DrawChart(chartItems, Modifier.fillMaxWidth().height(350.dp))
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun DrawChart(data: List<BudgetRemainingBudgetBarChartData>, modifier: Modifier) {
    val textMeasurer = rememberTextMeasurer()

    val maxBudget = data.flatMap { listOf(it.starting_budget, it.remaining_budget) }.maxOrNull() ?: 1.0
    val roundedMax = (ceil(maxBudget / 1000.0) * 1000.0).coerceAtLeast(1000.0)

    Canvas(modifier = modifier.padding(top = 16.dp, bottom = 48.dp, start = 64.dp, end = 16.dp)) {
        val chartAreaWidth = size.width
        val chartAreaHeight = size.height
        val barGroupWidth = if (data.isNotEmpty()) chartAreaWidth / data.size else chartAreaWidth
        val heightScale = chartAreaHeight / roundedMax.toFloat()
        val singleBarWidth = barGroupWidth / 4.0f

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
                topLeft = Offset(-55.dp.toPx(), y - 10.dp.toPx()),
                style = TextStyle(
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        data.forEachIndexed { index, item ->
            val startHeight = (item.starting_budget * heightScale).toFloat()
            val remainingHeight = (item.remaining_budget * heightScale).toFloat()

            val barX = index * barGroupWidth
            val totalBarPairWidth = 2 * singleBarWidth
            val centeringSpace = (barGroupWidth - totalBarPairWidth) / 2
            val startX = barX + centeringSpace

            drawRect(
                color = Color(0xFF8884d8),
                topLeft = Offset(startX, chartAreaHeight - startHeight),
                size = Size(singleBarWidth, startHeight)
            )

            drawRect(
                color = Color(0xFF82ca9d),
                topLeft = Offset(startX + singleBarWidth, chartAreaHeight - remainingHeight),
                size = Size(singleBarWidth, remainingHeight)
            )

            val labelResult = textMeasurer.measure(
                text = AnnotatedString(item.name),
                style = TextStyle(color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            )

            drawText(
                textLayoutResult = labelResult,
                topLeft = Offset(
                    x = barX + (barGroupWidth - labelResult.size.width) / 2,
                    y = chartAreaHeight + 12.dp.toPx()
                )
            )
        }
    }
}