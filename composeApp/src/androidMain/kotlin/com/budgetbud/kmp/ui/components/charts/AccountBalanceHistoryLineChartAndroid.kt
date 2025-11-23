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
import androidx.compose.ui.graphics.nativeCanvas
import com.budgetbud.kmp.models.AccountBalanceChartData
import com.budgetbud.kmp.models.AccountData
import com.budgetbud.kmp.models.AccountOverviewData
import com.budgetbud.kmp.auth.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import java.text.SimpleDateFormat
import java.util.*


@Composable
actual fun AccountBalanceHistoryLineChart(
    xSizePercent: Int,
    ySizePercent: Int,
    apiClient: ApiClient,
    familyView: Boolean,
    modifier: Modifier
) {
    var chartData by remember { mutableStateOf<AccountBalanceChartData?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val proportionalModifier = modifier
        .fillMaxWidth(xSizePercent / 100f)
        .fillMaxHeight(ySizePercent / 100f)
        .aspectRatio(1f)

    LaunchedEffect(familyView) {
        isLoading = true
        try {
            val tokens = apiClient.getTokens()
            val accounts = apiClient.client.get("https://api.budgetingbud.com/api/accounts/") {
                parameter("familyView", familyView)
                headers {
                    tokens?.let {
                        append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                    }
                }
            }.body<List<AccountData>>()

            val history = apiClient.client.get("https://api.budgetingbud.com/api/accounts/overview-report/") {
                parameter("familyView", familyView)
                headers {
                    tokens?.let {
                        append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                    }
                }
            }.body<List<AccountOverviewData>>()

            val dataMax: Double = history
                .maxOfOrNull { it.balances["Checking"]?.toDoubleOrNull() ?: 0.0 }
                ?: 1000.0

            chartData = AccountBalanceChartData(accounts, history, dataMax)
        } catch (e: Exception) {
            error = "Failed to fetch account chart data"
        } finally {
            isLoading = false
        }
    }

    chartData?.let { data ->
        ChartCanvas(data = data, modifier = proportionalModifier)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }

    error?.let {
        Text(
            text = it,
            color = Color.Red,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ChartCanvas(data: AccountBalanceChartData, modifier: Modifier) {
    val accounts = data.accounts
    val history = data.history
    val dataMax = data.dataMax

    val inputDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    val sortedDates = remember(history) {
        history.mapNotNull {
            try {
                inputDateFormat.parse(it.name)
            } catch (_: Exception) {
                null
            }
        }.distinct().sorted()
    }

    val dataSeries = remember(accounts, history, sortedDates) {
        accounts.associate { account ->
            val balancesByDate = sortedDates.map { date ->
                val dateStr = inputDateFormat.format(date)
                val entry = history.find { it.name == dateStr }
                val value = entry?.balances?.get(account.name)?.toFloatOrNull() ?: 0f
                value
            }
            account.name to balancesByDate
        }
    }

    val colors = listOf(
        Color(0xFF1DB954), Color(0xFF6200EE), Color(0xFFFF5722),
        Color(0xFF03A9F4), Color(0xFFFFC107), Color(0xFF795548), Color(0xFF009688)
    )

    var tooltipData by remember { mutableStateOf<Pair<Date, Map<String, Float>>?>(null) }
    var tooltipOffset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.padding(horizontal = 48.dp, vertical = 32.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (sortedDates.size > 1) {
                            val stepX = size.width / (sortedDates.size - 1)
                            val index = (offset.x / stepX).toInt().coerceIn(0, sortedDates.lastIndex)
                            val selectedDate = sortedDates[index]

                            val valuesAtDate = dataSeries.mapValues { (_, balances) ->
                                balances.getOrNull(index) ?: 0f
                            }

                            tooltipData = selectedDate to valuesAtDate
                            tooltipOffset = offset
                        }
                    }
                }
        ) {
            if (sortedDates.size < 2) {
                drawContext.canvas.nativeCanvas.drawText(
                    "Insufficient data to display chart",
                    center.x,
                    center.y,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 40f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
                return@Canvas
            }

            val stepX = size.width / (sortedDates.size - 1)
            val yScale = size.height / dataMax

            repeat(5) { i ->
                val y = i * size.height / 5
                drawLine(Color.LightGray, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                drawContext.canvas.nativeCanvas.drawText(
                    "$${dataMax - i * dataMax / 5}",
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

            dataSeries.entries.forEachIndexed { index, (_, balances) ->
                val color = colors[index % colors.size]
                if (balances.size < 2) return@forEachIndexed

                val path = Path()
                val points = balances.mapIndexed { i, value ->
                    Offset(
                        x = (i * stepX),
                        y = (size.height - (value * yScale)).toFloat()
                    )
                }

                path.moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { path.lineTo(it.x, it.y) }

                drawPath(path, color, style = Stroke(width = 4f))
                points.forEach { drawCircle(color, radius = 8f, center = it) }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
        ) {
            accounts.forEachIndexed { index, account ->
                val color = colors[index % colors.size]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(Modifier.size(12.dp).background(color))
                    Spacer(Modifier.width(4.dp))
                    Text(account.name, style = MaterialTheme.typography.labelMedium)
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
                    values.forEach { (name, amount) ->
                        val color = colors[accounts.indexOfFirst { it.name == name }.coerceAtLeast(0) % colors.size]
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(color))
                            Spacer(Modifier.width(6.dp))
                            Text("$name: $${"%.2f".format(amount)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}