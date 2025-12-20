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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.CategoryHistoryLineChartData
import com.budgetbud.kmp.models.CategoryOverviewData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import java.text.SimpleDateFormat
import java.util.*
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

    val fixedChartHeight = Modifier.height(300.dp)

    LaunchedEffect(familyView) {
        onLoadingStatusChange(true)
        try {
            val tokens = apiClient.getTokens()
            val categories = apiClient.client.get("https://api.budgetingbud.com/api/category/data/") {
                parameter("familyView", familyView)
                headers {
                    tokens?.let {
                        append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                    }
                }
            }.body<List<CategoryOverviewData>>()

            val history = apiClient.client.get("https://api.budgetingbud.com/api/category/history/line-chart/") {
                parameter("familyView", familyView)
                headers {
                    tokens?.let {
                        append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                    }
                }
            }.body<List<CategoryHistoryLineChartData>>()

            categoryData = categories
            historyData = history
        } catch (e: Exception) {
            error = "Failed to fetch category chart data"
        } finally {
            onLoadingStatusChange(false)
        }
    }

    when {
        error != null -> {
            Text(
                text = error ?: "Unknown error",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier.padding(16.dp)
            )
        }

        categoryData.isNotEmpty() && historyData.isNotEmpty() -> {

            Text(
                text = "Category Expense Line Chart",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CategoryExpenseChartCanvas(
                categoryData = categoryData,
                historyData = historyData,
                modifier = modifier.then(fixedChartHeight)
            )
        }
    }
}

@Composable
private fun CategoryExpenseChartCanvas(
    categoryData: List<CategoryOverviewData>,
    historyData: List<CategoryHistoryLineChartData>,
    modifier: Modifier
) {
    val inputDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    val categoryNames = categoryData.map { it.name }

    val parsedData = remember(historyData) {
        historyData.mapNotNull { entry ->
            val date = try {
                inputDateFormat.parse(entry.name)
            } catch (e: Exception) {
                null
            }
            date?.let { it to entry.balances }
        }
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
    val calculatedRoundedMax = ceil(maxDataValue / 1000.0) * 1000.0
    val roundedMax = if (calculatedRoundedMax <= 0.0) 1000.0f else calculatedRoundedMax.toFloat()

    val colors = listOf(
        Color(0xFF1DB954), Color(0xFF6200EE), Color(0xFFFF5722),
        Color(0xFF03A9F4), Color(0xFFFFC107), Color(0xFF795548), Color(0xFF009688)
    )

    var tooltipData by remember { mutableStateOf<Pair<Date, Map<String, Float>>?>(null) }
    var tooltipOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 72.dp, start = 80.dp, end = 32.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (sortedDates.isNotEmpty()) {
                            val stepX = if (sortedDates.size > 1) size.width / (sortedDates.size - 1) else size.width
                            val index = if (sortedDates.size > 1) {
                                (offset.x / stepX).toInt().coerceIn(0, sortedDates.lastIndex)
                            } else 0

                            val selectedDate = sortedDates[index]
                            val valuesAtDate = categoryNames.associateWithNotNull { category ->
                                dataSeries[category]?.find { it.first == selectedDate }?.second
                            }

                            tooltipData = selectedDate to valuesAtDate
                            tooltipOffset = offset
                        }
                    }
                }
        ) {
            if (sortedDates.isEmpty()) return@Canvas

            val stepX = if (sortedDates.size > 1) size.width / (sortedDates.size - 1) else size.width / 2
            val yScale = size.height / roundedMax
            val labelTextSize = 30f
            val numDivisions = 5

            repeat(numDivisions + 1) { i ->
                val y = i * size.height / numDivisions
                drawLine(Color.LightGray, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)

                val labelValue = roundedMax - (i * roundedMax / numDivisions)

                drawContext.canvas.nativeCanvas.drawText(
                    "$${"%.0f".format(labelValue)}",
                    -90f,
                    y + 10,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = labelTextSize
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )
            }

            sortedDates.forEachIndexed { i, date ->
                if (i % 2 == 0) {
                    val x = if (sortedDates.size > 1) i * stepX else size.width / 2
                    drawContext.canvas.nativeCanvas.apply {
                        save()
                        rotate(-45f, x, size.height + 65)
                        drawText(
                            displayDateFormat.format(date),
                            x,
                            size.height + 65,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.DKGRAY
                                textSize = labelTextSize
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                        restore()
                    }
                }
            }

            dataSeries.entries.forEachIndexed { index, (_, points) ->
                val color = colors[index % colors.size]
                if (points.isEmpty()) return@forEachIndexed

                val offsets = points.map { (date, value) ->
                    val x = if (sortedDates.size > 1) sortedDates.indexOf(date) * stepX else size.width / 2
                    val y = size.height - (value * yScale)
                    Offset(x, y)
                }

                if (offsets.size >= 2) {
                    val path = Path()
                    path.moveTo(offsets.first().x, offsets.first().y)
                    offsets.drop(1).forEach { path.lineTo(it.x, it.y) }
                    drawPath(path, color = color, style = Stroke(width = 4f))
                }

                offsets.forEach { center ->
                    drawCircle(color = color, radius = 8f, center = center)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            categoryNames.forEachIndexed { index, name ->
                val color = colors[index % colors.size]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(Modifier.size(12.dp).background(color, MaterialTheme.shapes.small))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(name, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        tooltipData?.let { (date, values) ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = tooltipOffset.x.dp, y = (tooltipOffset.y - 80).dp),
                shadowElevation = 6.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Date: ${displayDateFormat.format(date)}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    values.forEach { (category, amount) ->
                        val color = colors[categoryNames.indexOf(category) % colors.size]
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(color, MaterialTheme.shapes.small))
                            Spacer(modifier = Modifier.width(6.dp))
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