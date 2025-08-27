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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.AccountBalanceChartData
import androidx.compose.ui.graphics.nativeCanvas
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AccountBalanceLineChart(
    chartData: AccountBalanceChartData,
    modifier: Modifier = Modifier
) {
    val accounts = chartData.accounts
    val history = chartData.history
    val dataMax = chartData.dataMax.toFloat()

    if (accounts.isEmpty() || history.isEmpty()) return

    val inputDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    val sortedDates = remember(history) {
        history.mapNotNull { entry ->
            try {
                inputDateFormat.parse(entry.name)
            } catch (e: Exception) {
                null
            }
        }.distinct().sorted()
    }

    val dataSeries = remember(accounts, history, sortedDates) {
        accounts.associate { account ->
            val name = account.name
            val balancesByDate = sortedDates.map { date ->
                val dateStr = inputDateFormat.format(date)
                val entry = history.find { it.name == dateStr }
                val balanceStr = entry?.balances?.get(name)
                balanceStr?.toFloatOrNull() ?: 0f
            }
            name to balancesByDate
        }
    }

    val yRange = if (dataMax == 0f) 1f else dataMax

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

                    val valuesAtDate = dataSeries.mapValues { (_, balances) ->
                        balances.getOrNull(index) ?: 0f
                    }

                    tooltipData = selectedDate to valuesAtDate
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
                    String.format("$%.0f", dataMax - i * yRange / gridLines),
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

            dataSeries.entries.forEachIndexed { index, (accountName, balances) ->
                val color = colors[index % colors.size]
                if (balances.size < 2) return@forEachIndexed

                val path = Path()
                val points = balances.mapIndexed { i, value ->
                    Offset(
                        x = i * stepX,
                        y = size.height - (value * yScale)
                    )
                }

                path.moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { path.lineTo(it.x, it.y) }

                drawPath(path, color = color, style = Stroke(width = 4f))
                points.forEach { drawCircle(color = color, radius = 8f, center = it) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            accounts.map { it.name }.forEachIndexed { index, name ->
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
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Date: ${displayDateFormat.format(date)}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    values.forEach { (accountName, amount) ->
                        val color = colors[accounts.indexOfFirst { it.name == accountName }.coerceAtLeast(0) % colors.size]
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(color))
                            Spacer(Modifier.width(6.dp))
                            Text("$accountName: $${"%.2f".format(amount)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
