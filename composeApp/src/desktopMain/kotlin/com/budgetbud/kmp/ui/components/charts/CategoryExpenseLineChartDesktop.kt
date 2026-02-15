package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.CategoryHistoryLineChartData
import com.budgetbud.kmp.models.CategoryOverviewData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import kotlinx.datetime.LocalDate
import kotlin.math.ceil

@Composable
actual fun CategoryExpenseLineChart(
    apiClient: ApiClient,
    familyView: Boolean,
    modifier: Modifier,
    onLoadingStatusChange: (isLoading: Boolean) -> Unit
) {
    var error by remember { mutableStateOf<String?>(null) }
    var categoryData by remember { mutableStateOf<List<CategoryOverviewData>>(emptyList()) }
    var historyData by remember { mutableStateOf<List<CategoryHistoryLineChartData>>(emptyList()) }

    LaunchedEffect(familyView) {
        onLoadingStatusChange(true)
        try {
            val tokens = apiClient.getTokens()
            val categories = apiClient.client.get("https://api.budgetingbud.com/api/category/data/") {
                parameter("familyView", familyView)
                headers { tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") } }
            }.body<List<CategoryOverviewData>>()

            val history = apiClient.client.get("https://api.budgetingbud.com/api/category/history/line-chart/") {
                parameter("familyView", familyView)
                headers { tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") } }
            }.body<List<CategoryHistoryLineChartData>>()

            categoryData = categories
            historyData = history
        } catch (e: Exception) {
            error = "Failed to fetch category chart data"
        } finally {
            onLoadingStatusChange(false)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (categoryData.isNotEmpty()) {
            Text(
                text = "Category Expense Line Chart",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        when {
            error != null -> Text(error!!, color = Color.Red, modifier = Modifier.padding(16.dp))
            categoryData.isNotEmpty() && historyData.isNotEmpty() -> {
                CategoryExpenseChartCanvas(
                    categoryData = categoryData,
                    historyData = historyData,
                    modifier = Modifier.height(400.dp).fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun CategoryExpenseChartCanvas(
    categoryData: List<CategoryOverviewData>,
    historyData: List<CategoryHistoryLineChartData>,
    modifier: Modifier
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val categoryNames = categoryData.map { it.name }

    val parsedData = remember(historyData) {
        historyData.map { entry -> LocalDate.parse(entry.name) to entry.balances }
    }

    val sortedDates = parsedData.map { it.first }.distinct().sorted()

    val dataSeries = remember(parsedData, categoryNames) {
        categoryNames.associateWith { categoryName ->
            parsedData.mapNotNull { (date, balances) ->
                balances[categoryName]?.let { value -> date to value }
            }
        }
    }

    val allYValues = dataSeries.values.flatten().map { it.second }
    val maxDataValue = allYValues.maxOrNull()?.toDouble() ?: 1000.0
    val roundedMax = (ceil(maxDataValue / 1000.0) * 1000.0).coerceAtLeast(1000.0).toFloat()

    val colors = listOf(
        Color(0xFF1DB954), Color(0xFF6200EE), Color(0xFFFF5722),
        Color(0xFF03A9F4), Color(0xFFFFC107), Color(0xFF795548), Color(0xFF009688)
    )

    var tooltipData by remember { mutableStateOf<Pair<LocalDate, Map<String, Float>>?>(null) }
    var tooltipOffsetPx by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.padding(16.dp).background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, bottom = 80.dp, start = 80.dp, end = 40.dp)
                .pointerInput(sortedDates) {
                    detectTapGestures { offset ->
                        if (sortedDates.isNotEmpty()) {
                            val divisor = (sortedDates.size - 1).coerceAtLeast(1)
                            val stepX = size.width / divisor
                            val index = (offset.x / stepX).toInt().coerceIn(0, sortedDates.lastIndex)
                            val selectedDate = sortedDates[index]
                            val valuesAtDate = categoryNames.associateWithNotNull { category ->
                                dataSeries[category]?.find { it.first == selectedDate }?.second
                            }
                            tooltipData = selectedDate to valuesAtDate
                            tooltipOffsetPx = offset
                        }
                    }
                }
        ) {
            if (sortedDates.isEmpty()) return@Canvas

            val divisor = (sortedDates.size - 1).coerceAtLeast(1)
            val stepX = size.width / divisor
            val yScale = size.height / roundedMax

            repeat(6) { i ->
                val y = i * size.height / 5
                drawLine(Color.LightGray.copy(alpha = 0.5f), Offset(0f, y), Offset(size.width, y))
                val label = "$${(roundedMax - (i * roundedMax / 5)).toInt()}"

                translate(left = -70f, top = y - 10f) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = label,
                        style = TextStyle(color = Color.Gray, fontSize = 10.sp)
                    )
                }
            }

            sortedDates.forEachIndexed { i, date ->
                if (i % 2 == 0) {
                    val x = i * stepX
                    translate(left = x, top = size.height + 25f) {
                        rotate(-45f, pivot = Offset.Zero) {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = "${date.month.name.take(3)} ${date.dayOfMonth}",
                                style = TextStyle(color = Color.DarkGray, fontSize = 10.sp)
                            )
                        }
                    }
                }
            }

            dataSeries.entries.forEachIndexed { index, (_, points) ->
                val color = colors[index % colors.size]
                val offsets = points.map { (date, value) ->
                    val x = if (sortedDates.size > 1) sortedDates.indexOf(date) * stepX else size.width / 2
                    Offset(x, size.height - (value * yScale))
                }

                if (offsets.size >= 2) {
                    val path = Path().apply {
                        moveTo(offsets.first().x, offsets.first().y)
                        offsets.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(path, color = color, style = Stroke(width = 3.dp.toPx()))
                }
                offsets.forEach { drawCircle(color = color, radius = 4.dp.toPx(), center = it) }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            categoryNames.forEachIndexed { index, name ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Box(Modifier.size(10.dp).background(colors[index % colors.size], MaterialTheme.shapes.small))
                    Spacer(Modifier.width(4.dp))
                    Text(name, style = MaterialTheme.typography.labelSmall, softWrap = false)
                }
            }
        }

        tooltipData?.let { (date, values) ->
            val tooltipX = with(density) { (80.dp.toPx() + tooltipOffsetPx.x).toDp() }
            val tooltipY = with(density) { (20.dp.toPx() + tooltipOffsetPx.y).toDp() }

            Surface(
                modifier = Modifier.offset(x = tooltipX, y = tooltipY - 100.dp),
                shadowElevation = 8.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${date.month.name.take(3)} ${date.dayOfMonth}, ${date.year}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    values.forEach { (category, amount) ->
                        val color = colors[categoryNames.indexOf(category) % colors.size]
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(color))
                            Spacer(Modifier.width(8.dp))
                            Text("$category: $${"%.2f".format(amount)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private inline fun <K, V> Iterable<K>.associateWithNotNull(transform: (K) -> V?): Map<K, V> {
    return buildMap {
        for (element in this@associateWithNotNull) {
            val value = transform(element)
            if (value != null) put(element, value)
        }
    }
}