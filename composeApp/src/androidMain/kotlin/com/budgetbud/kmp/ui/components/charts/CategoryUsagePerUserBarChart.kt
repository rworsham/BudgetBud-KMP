package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
    xSizePercent: Int,
    ySizePercent: Int,
    familyView: Boolean,
    apiClient: ApiClient,
    modifier: Modifier
) {
    var data by remember { mutableStateOf<List<FamilyCategoryOverviewData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(familyView) {
        isLoading = true
        try {
            val response: HttpResponse = apiClient.client.get("https://api.budgetingbud.com/api/family/overview/") {
                parameter("Category", true)
                contentType(ContentType.Application.Json)
            }

            val rawData = response.body<List<Map<String, Any>>>()

            data = rawData.map {
                FamilyCategoryOverviewData(
                    name = it["name"]?.toString() ?: "Unknown",
                    category = it["category"]?.toString() ?: "",
                    category_count = (it["category_count"] as? Number)?.toFloat() ?: 0f
                )
            }
        } catch (e: Exception) {
            error = "Failed to fetch account data"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth(xSizePercent / 100f)
            .heightIn(min = (ySizePercent * 10).dp)
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            error != null -> Text(error ?: "Error", color = Color.Red)
            data.isEmpty() -> Text("No data available")
            else -> FamilyBarChart(data)
        }
    }
}

@Composable
private fun FamilyBarChart(data: List<FamilyCategoryOverviewData>) {
    val maxCount = data.maxOfOrNull { it.category_count } ?: 1f
    val roundedMax = ceil(maxCount / 10f) * 10f

    Column(modifier = Modifier.fillMaxWidth()) {
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
                val barHeight = item.category_count * heightScale
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = Color(0xFF8884D8),
                    topLeft = Offset(xOffset, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            data.forEach {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    maxLines = 1
                )
            }
        }
    }
}
