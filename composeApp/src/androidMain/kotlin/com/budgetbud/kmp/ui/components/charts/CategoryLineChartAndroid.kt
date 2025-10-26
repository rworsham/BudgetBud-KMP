package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.nativeCanvas
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.CategoryHistoryLineChartData
import com.budgetbud.kmp.models.CategoryOverviewData
import java.text.SimpleDateFormat
import java.util.*

@Composable
actual fun CategoryLineChart(
    historyData: List<CategoryHistoryLineChartData>,
    categoryData: List<CategoryOverviewData>,
    modifier: Modifier
) {
    if (historyData.isEmpty() || categoryData.isEmpty()) return

    val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

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
    val minY = allYValues.minOrNull() ?: 0f
    val maxY = allYValues.maxOrNull() ?: 1f
    val yRange = if ((maxY - minY) == 0f) 1f else maxY - minY

    val colors = listOf(
        Color(0xFF1DB954),
        Color(0xFF6200EE),
        Color(0xFFFF5722),
        Color(0xFF03A9F4),
        Color(0xFFFFC107),
        Color(0xFF795548),
        Color(0xFF009688)
    )

    var tooltipData by remember { mutableStateOf<Pair<Date, Map<String, Float>>?>(null) }
    var tooltipOffset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.padding(horizontal = 48.dp, vertical = 32.dp)) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val stepX = size.width / (sortedDates.size - 1)
                    val index = ((offset.x / stepX).toInt()).coerceIn(0, sortedDates.size - 1)
                    val selectedDate = sortedDates[index]

                    val values = categoryNames.associateWithNotNull { cat ->
                        dataSeries[cat]?.find { it.first == selectedDate }?.second
                    }

                    tooltipData = selectedDate to values
                    tooltipOffset = offset
                }
            }
        ) {
            val stepX = size.width / (sortedDates.size - 1)
            val yScale = size.height / yRange

            val gridLines = 5
            for (i in 0..gridLines) {
                val y = i * size.height / gridLines
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%.2f", maxY - i * yRange / gridLines),
                    0f,
                    y + 14,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 30f
                    }
                )
            }

            sortedDates.forEachIndexed { i, date ->
                val x = i * stepX
                drawContext.canvas.nativeCanvas.apply {
                    save()
                    rotate(-45f, x, size.height + 20)
                    drawText(
                        displayDateFormat.format(date),
                        x,
                        size.height + 20,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.DKGRAY
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                    restore()
                }
            }

            dataSeries.entries.forEachIndexed { index, (category, points) ->
                val color = colors[index % colors.size]
                if (points.size < 2) return@forEachIndexed

                val path = Path()
                val offsets = points.map { (date, value) ->
                    val x = sortedDates.indexOf(date) * stepX
                    val y = size.height - ((value - minY) * yScale)
                    Offset(x, y)
                }

                path.moveTo(offsets.first().x, offsets.first().y)
                offsets.drop(1).forEach { path.lineTo(it.x, it.y) }

                drawPath(path, color = color, style = Stroke(width = 4f))
                offsets.forEach { drawCircle(color = color, radius = 8f, center = it) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            categoryNames.forEachIndexed { index, name ->
                val color = colors[index % colors.size]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(modifier = Modifier.size(12.dp).background(color))
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
                            Box(modifier = Modifier.size(10.dp).background(color))
                            Spacer(Modifier.width(6.dp))
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
