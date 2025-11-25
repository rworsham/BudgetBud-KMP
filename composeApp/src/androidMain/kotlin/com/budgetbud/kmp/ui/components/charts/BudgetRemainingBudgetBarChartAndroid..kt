package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.models.BudgetRemainingBudgetBarChartData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.math.ceil
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

@Composable
actual fun BudgetRemainingBudgetBarChart(
    startDate: String,
    endDate: String,
    familyView: Boolean,
    modifier: Modifier,
    apiClient: ApiClient
) {
    var chartItems by remember { mutableStateOf<List<BudgetRemainingBudgetBarChartData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(startDate, endDate, familyView) {
        isLoading = true
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
            error = "Error fetching chart data"
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            CircularProgressIndicator()
        }
        error != null -> {
            Text(text = error!!, color = Color.Red)
        }
        chartItems.isEmpty() -> {
            Text("No data available")
        }
        else -> {
            DrawChart(chartItems, modifier)
        }
    }
}

@Composable
private fun DrawChart(data: List<BudgetRemainingBudgetBarChartData>, modifier: Modifier) {
    val maxBudget = data.flatMap { listOf(it.starting_budget, it.remaining_budget) }.maxOrNull() ?: 1.0
    val roundedMax = ceil(maxBudget / 1000.0) * 1000.0

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 16.dp)
        ) {
            val barGroupWidth = size.width / data.size
            val heightScale = size.height / roundedMax

            data.forEachIndexed { index, item ->
                val startHeight = (item.starting_budget * heightScale).toFloat()
                val remainHeight = (item.remaining_budget * heightScale).toFloat()
                val barX = index * barGroupWidth
                val barWidth = barGroupWidth / 2.5f

                drawRect(
                    color = Color(0xFF8884d8),
                    topLeft = Offset(barX + barWidth * 0.25f, size.height - startHeight),
                    size = Size(barWidth, startHeight)
                )
                drawRect(
                    color = Color(0xFF82ca9d),
                    topLeft = Offset(barX + barWidth * 1.25f, size.height - remainHeight),
                    size = Size(barWidth, remainHeight)
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
