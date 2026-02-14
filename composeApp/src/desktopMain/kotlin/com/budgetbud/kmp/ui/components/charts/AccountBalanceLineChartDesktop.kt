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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetbud.kmp.models.AccountBalanceChartData
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalTextApi::class)
@Composable
actual fun AccountBalanceLineChart(
    chartData: AccountBalanceChartData,
    modifier: Modifier
) {
    val accounts = chartData.accounts
    val history = chartData.history
    val dataMax = chartData.dataMax.toFloat()

    if (accounts.isEmpty() || history.isEmpty()) return

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val sortedDates = remember(history) {
        history.map { LocalDate.parse(it.name) }.sorted()
    }

    val dataSeries = remember(accounts, history, sortedDates) {
        accounts.associate { account ->
            val name = account.name
            val balancesByDate = sortedDates.map { date ->
                val entry = history.find { it.name == date.toString() }
                entry?.balances?.get(name)?.toFloatOrNull() ?: 0f
            }
            name to balancesByDate
        }
    }

    val yRange = if (dataMax == 0f) 1000f else dataMax
    val colors = listOf(Color(0xFF1DB954), Color(0xFF6200EE), Color(0xFF009688))

    var tooltipData by remember { mutableStateOf<Pair<LocalDate, Map<String, Float>>?>(null) }
    var tooltipOffsetPx by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.padding(start = 60.dp, end = 24.dp, top = 32.dp, bottom = 64.dp)) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .pointerInput(sortedDates) {
                detectTapGestures { offset ->
                    val divisor = (sortedDates.size - 1).coerceAtLeast(1)
                    val stepX = size.width / divisor
                    val index = (offset.x / stepX).toInt().coerceIn(0, sortedDates.size - 1)
                    tooltipData = sortedDates[index] to dataSeries.mapValues { it.value[index] }
                    tooltipOffsetPx = offset
                }
            }
        ) {
            val divisor = (sortedDates.size - 1).coerceAtLeast(1)
            val stepX = size.width / divisor
            val yScale = size.height / yRange

            val gridLines = 5
            for (i in 0..gridLines) {
                val y = i * size.height / gridLines
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )

                val labelValue = (dataMax - i * dataMax / gridLines).toInt().toString()
                translate(left = -50.dp.toPx(), top = y - 10.dp.toPx()) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "$$labelValue",
                        style = TextStyle(color = Color.Gray, fontSize = 10.sp)
                    )
                }
            }

            sortedDates.forEachIndexed { i, date ->
                if (i % 2 == 0) {
                    val x = i * stepX
                    val dateText = "${date.month.name.take(3)} ${date.dayOfMonth}"

                    translate(left = x, top = size.height + 20f) {
                        rotate(degrees = -45f, pivot = Offset.Zero) {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = dateText,
                                style = TextStyle(color = Color.DarkGray, fontSize = 10.sp)
                            )
                        }
                    }
                }
            }

            dataSeries.entries.forEachIndexed { index, (_, balances) ->
                val color = colors[index % colors.size]
                if (balances.size < 2) return@forEachIndexed

                val path = Path()
                val points = balances.mapIndexed { i, value ->
                    Offset(x = i * stepX, y = size.height - (value * yScale))
                }

                path.moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { path.lineTo(it.x, it.y) }

                drawPath(path, color = color, style = Stroke(width = 3.dp.toPx()))
                points.forEach { drawCircle(color = color, radius = 4.dp.toPx(), center = it) }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            accounts.forEachIndexed { index, account ->
                val color = colors[index % colors.size]
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(color))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(account.name, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        tooltipData?.let { (date, values) ->
            val offsetX = with(density) { tooltipOffsetPx.x.toDp() }
            val offsetY = with(density) { tooltipOffsetPx.y.toDp() }

            Surface(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY - 100.dp),
                shadowElevation = 8.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Date: $date", style = MaterialTheme.typography.labelLarge)
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    values.forEach { (accountName, amount) ->
                        val color = colors[accounts.indexOfFirst { it.name == accountName }.coerceAtLeast(0) % colors.size]
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(color))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$accountName: $${(amount.toInt())}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}