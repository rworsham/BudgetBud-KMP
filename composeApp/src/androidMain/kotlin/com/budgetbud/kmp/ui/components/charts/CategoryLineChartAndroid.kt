package com.budgetbud.kmp.ui.components.charts

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.CategoryHistoryLineChartData
import com.budgetbud.kmp.models.CategoryOverviewData
import java.text.SimpleDateFormat
import java.util.*
import java.text.NumberFormat

@SuppressLint("DefaultLocale")
@Composable
actual fun CategoryLineChart(
    historyData: List<CategoryHistoryLineChartData>,
    categoryData: List<CategoryOverviewData>,
    modifier: Modifier
) {
    if (historyData.isEmpty() || categoryData.isEmpty()) return

    val density = LocalDensity.current
    val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    val categoryNames = categoryData.map { it.name }

    val chronologicalEntries = remember(historyData) {
        historyData.mapNotNull { entry ->
            val date = try { inputDateFormat.parse(entry.name) } catch (e: Exception) { null }
            val balancesMap = entry.balances.mapValues { (_, valueStr) ->
                parseBalanceToFloat(valueStr)
            }
            date?.let { it to balancesMap }
        }
    }

    val chronologicalDates = chronologicalEntries.map { it.first }

    val filledDataSeries = remember(chronologicalEntries, chronologicalDates, categoryNames) {
        categoryNames.associateWith { categoryName ->
            val series = mutableListOf<Pair<Date, Float>>()
            var lastValue: Float? = null

            for ((date, dayData) in chronologicalEntries) {
                val currentValue = dayData[categoryName]

                if (currentValue != null) {
                    lastValue = currentValue
                }

                if (lastValue != null) {
                    series.add(date to lastValue)
                }
            }
            series
        }
    }

    val allYValues = filledDataSeries.values.flatten().map { it.second }
    val minY = allYValues.minOrNull() ?: 0f
    val actualMaxY = allYValues.maxOrNull() ?: 1f

    val chartCeilingY = (actualMaxY * 1.5f).coerceAtLeast(1f)

    val yRange = chartCeilingY - minY
    val effectiveYRange = if (yRange == 0f) 1f else yRange


    val colors = listOf(
        Color(0xFF1DB954),
        Color(0xFF6200EE),
        Color(0xFF009688)
    )

    var tooltipData by remember { mutableStateOf<Pair<Date, Map<String, Float>>?>(null) }
    var tooltipOffsetPx by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .pointerInput(chronologicalDates) {
                detectTapGestures { offset ->
                    val stepX = size.width / (chronologicalDates.size - 1).coerceAtLeast(1)

                    val nearestXIndex = (offset.x / stepX)
                        .coerceIn(0f, (chronologicalDates.size - 1).toFloat())
                        .toInt()

                    val selectedDate = chronologicalDates[nearestXIndex]

                    val values = categoryNames.associateWithNotNull { cat ->
                        filledDataSeries[cat]?.find { it.first == selectedDate }?.second
                    }

                    tooltipData = selectedDate to values

                    val xPos = nearestXIndex * stepX.toFloat()
                    tooltipOffsetPx = Offset(xPos, offset.y)
                }
            }
        ) {
            val stepX = size.width / (chronologicalDates.size - 1).coerceAtLeast(1)
            val yScale = size.height / effectiveYRange
            val Y_AXIS_TEXT_OFFSET = 4.dp.toPx()

            val yAxisTextPaint = Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 25f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }

            val xAxisTextPaint = Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 25f
                textAlign = Paint.Align.CENTER
            }

            val gridLines = 5
            for (i in 0..gridLines) {
                val y = i * size.height / gridLines

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )

                val value = chartCeilingY - i * effectiveYRange / gridLines

                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%,.2f", value),
                    Y_AXIS_TEXT_OFFSET,
                    y + 10f,
                    yAxisTextPaint
                )
            }

            chronologicalDates.forEachIndexed { i, date ->
                val x = i * stepX

                if (i > 0) {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                }

                if (i % 2 == 0) {
                    drawContext.canvas.nativeCanvas.apply {
                        save()
                        rotate(-45f, x, size.height + 20)
                        drawText(
                            displayDateFormat.format(date),
                            x,
                            size.height + 20,
                            xAxisTextPaint
                        )
                        restore()
                    }
                }
            }

            filledDataSeries.entries.forEachIndexed { index, (_, points) ->
                val color = colors[index % colors.size]
                if (points.size < 2) return@forEachIndexed

                val path = Path()
                val offsets = points.mapNotNull { (date, value) ->
                    val xIndex = chronologicalDates.indexOf(date)
                    if (xIndex >= 0) {
                        val x = xIndex * stepX
                        val y = size.height - ((value - minY) * yScale)
                        Offset(x, y)
                    } else {
                        null
                    }
                }

                if (offsets.isNotEmpty()) {
                    path.moveTo(offsets.first().x, offsets.first().y)
                    offsets.drop(1).forEach { path.lineTo(it.x, it.y) }

                    drawPath(path, color = color, style = Stroke(width = 4f))
                    offsets.forEach { drawCircle(color = color, radius = 8f, center = it) }
                }
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
            val offsetX_dp = with(density) { tooltipOffsetPx.x.toDp() }
            val offsetY_dp = with(density) { tooltipOffsetPx.y.toDp() }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = offsetX_dp - 50.dp, y = offsetY_dp - 100.dp),
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

private fun parseBalanceToFloat(value: Any?): Float? {
    return when (value) {
        is String -> {
            try {
                NumberFormat.getInstance(Locale.US).parse(value)?.toFloat()
            } catch (e: Exception) {
                value.toFloatOrNull()
            }
        }
        is Float -> value
        else -> null
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