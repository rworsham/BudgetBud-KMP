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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetbud.kmp.models.CategoryHistoryLineChartData
import com.budgetbud.kmp.models.CategoryOverviewData
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalTextApi::class)
@Composable
actual fun CategoryLineChart(
    historyData: List<CategoryHistoryLineChartData>,
    categoryData: List<CategoryOverviewData>,
    modifier: Modifier
) {
    if (historyData.isEmpty() || categoryData.isEmpty()) return

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val categoryNames = categoryData.map { it.name }

    val chronologicalEntries = remember(historyData) {
        historyData.mapNotNull { entry ->
            try {
                LocalDate.parse(entry.name) to entry.balances
            } catch (e: Exception) {
                null
            }
        }
    }

    if (chronologicalEntries.isEmpty()) return

    val chronologicalDates = chronologicalEntries.map { it.first }

    val dataSeries = remember(chronologicalEntries, categoryNames) {
        categoryNames.associateWith { categoryName ->
            chronologicalEntries.mapNotNull { (date, dayData) ->
                dayData[categoryName]?.let { date to it }
            }
        }
    }

    val allYValues = dataSeries.values.flatten().map { it.second }
    val actualMaxY = allYValues.maxOrNull() ?: 1f
    val chartCeilingY = (actualMaxY * 1.5f).coerceAtLeast(1f)

    val colors = listOf(
        Color(0xFF1DB954),
        Color(0xFF6200EE),
        Color(0xFF009688)
    )

    var tooltipData by remember { mutableStateOf<Pair<LocalDate, Map<String, Float>>?>(null) }
    var tooltipOffsetPx by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .pointerInput(chronologicalDates) {
                detectTapGestures { offset ->
                    val divisor = (chronologicalDates.size - 1).coerceAtLeast(1)
                    val stepX = size.width / divisor
                    val nearestXIndex = (offset.x / stepX).toInt().coerceIn(0, chronologicalDates.size - 1)
                    val selectedDate = chronologicalDates[nearestXIndex]

                    val values = categoryNames.associateWithNotNull { cat ->
                        dataSeries[cat]?.find { it.first == selectedDate }?.second
                    }

                    if (values.isNotEmpty()) {
                        tooltipData = selectedDate to values
                        tooltipOffsetPx = Offset(nearestXIndex.toFloat() * stepX, offset.y)
                    } else {
                        tooltipData = null
                    }
                }
            }
        ) {
            val divisor = (chronologicalDates.size - 1).coerceAtLeast(1)
            val stepX = size.width / divisor
            val yScale = size.height / chartCeilingY

            val gridLines = 5
            for (i in 0..gridLines) {
                val y = i * size.height / gridLines
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )

                val value = chartCeilingY - i * chartCeilingY / gridLines
                translate(left = 4.dp.toPx(), top = (y - 12.dp.toPx()).coerceAtLeast(0f)) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = value.toMoneyString(),
                        style = TextStyle(color = Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }

            chronologicalDates.forEachIndexed { i, date ->
                val x = if (chronologicalDates.size == 1) size.width / 2 else i * stepX
                if (i % 2 == 0) {
                    translate(left = x, top = size.height + 15.dp.toPx()) {
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
                val path = Path()
                val offsets = points.mapNotNull { (date, value) ->
                    val xIndex = chronologicalDates.indexOf(date)
                    if (xIndex >= 0) {
                        val xPos = if (chronologicalDates.size == 1) size.width / 2 else xIndex * stepX
                        Offset(xPos, size.height - (value * yScale))
                    } else null
                }

                if (offsets.isNotEmpty()) {
                    if (offsets.size > 1) {
                        path.moveTo(offsets.first().x, offsets.first().y)
                        offsets.drop(1).forEach { path.lineTo(it.x, it.y) }
                        drawPath(path, color = color, style = Stroke(width = 2.dp.toPx()))
                    }
                    offsets.forEach { drawCircle(color = color, radius = 4.dp.toPx(), center = it) }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            categoryNames.forEachIndexed { index, name ->
                val color = colors[index % colors.size]
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(color))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(name, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        tooltipData?.let { (date, values) ->
            val offsetX = with(density) { tooltipOffsetPx.x.toDp() }
            val offsetY = with(density) { tooltipOffsetPx.y.toDp() }

            Surface(
                modifier = Modifier
                    .offset(
                        x = offsetX.coerceAtMost(300.dp),
                        y = (offsetY - 80.dp).coerceAtLeast(0.dp)
                    ),
                shadowElevation = 8.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Date: $date", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    values.forEach { (category, amount) ->
                        val color = colors[categoryNames.indexOf(category) % colors.size]
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(color))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$category: $${amount.toMoneyString()}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private fun Float.toMoneyString(): String {
    val rounded = (this * 100).toInt() / 100.0
    return rounded.toString()
}

private inline fun <K, V> Iterable<K>.associateWithNotNull(transform: (K) -> V?): Map<K, V> {
    return buildMap {
        for (element in this@associateWithNotNull) {
            val value = transform(element)
            if (value != null) put(element, value)
        }
    }
}