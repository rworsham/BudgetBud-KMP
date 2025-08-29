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
import com.budgetbud.kmp.models.ExpenseCategoryBarChartData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.math.ceil
import kotlinx.coroutines.launch

@Composable
actual fun ExpenseCategoriesBarChart(
    startDate: String,
    endDate: String,
    familyView: Boolean,
    modifier: Modifier,
    apiClient: ApiClient
) {
    var chartData by remember { mutableStateOf<List<ExpenseCategoryBarChartData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(startDate, endDate, familyView) {
        isLoading = true
        try {
            val response: HttpResponse = apiClient.client.post("https://api.budgetingbud.com/api/transaction-bar-chart/") {
                parameter("familyView", familyView)
                contentType(ContentType.Application.Json)
                setBody(mapOf("start_date" to startDate, "end_date" to endDate))
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
            isLoading = false
        }
    }

    when {
        isLoading -> CircularProgressIndicator()
        error != null -> Text(error ?: "", color = Color.Red)
        chartData.isEmpty() -> Text("No data available")
        else -> DrawExpenseChart(chartData, modifier)
    }
}

@Composable
private fun DrawExpenseChart(data: List<ExpenseCategoryBarChartData>, modifier: Modifier) {
    val maxAmount = data.maxOfOrNull { it.total_amount } ?: 1f
    val roundedMax = ceil(maxAmount / 1000f) * 1000f

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 16.dp)
        ) {
            val barWidth = size.width / data.size * 0.6f
            val spaceBetween = size.width / data.size
            val heightScale = size.height / roundedMax

            data.forEachIndexed { index, item ->
                val barHeight = item.total_amount * heightScale
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = Color(0xFF1DB954),
                    topLeft = Offset(xOffset, size.height - barHeight),
                    size = Size(barWidth, barHeight)
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
                    text = it.category,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
        }
    }
}
