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
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetbud.kmp.models.AccountBalanceChartData
import com.budgetbud.kmp.models.AccountData
import com.budgetbud.kmp.models.AccountOverviewData
import com.budgetbud.kmp.auth.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import kotlinx.datetime.LocalDate
import kotlin.math.ceil

@Composable
actual fun AccountBalanceHistoryLineChart(
    apiClient: ApiClient,
    familyView: Boolean,
    modifier: Modifier,
    onLoadingStatusChange: (isLoading: Boolean) -> Unit
) {
    var chartData by remember { mutableStateOf<AccountBalanceChartData?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val fixedChartHeight = Modifier.height(400.dp)

    LaunchedEffect(familyView) {
        onLoadingStatusChange(true)
        try {
            val tokens = apiClient.getTokens()
            val accounts = apiClient.client.get("https://api.budgetingbud.com/api/accounts/") {
                parameter("familyView", familyView)
                headers {
                    tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") }
                }
            }.body<List<AccountData>>()

            val rawHistoryMaps = apiClient.client.get("https://api.budgetingbud.com/api/accounts/overview-report/") {
                parameter("familyView", familyView)
                headers {
                    tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") }
                }
            }.body<List<Map<String, String?>>>()

            val history = rawHistoryMaps.map { map ->
                val name = map["name"] ?: throw IllegalStateException("History entry missing 'name'")
                AccountOverviewData(name = name, balances = map.filterKeys { it != "name" })
            }

            val dataMax = history.maxOfOrNull {
                it.balances.values.mapNotNull { b -> b?.toDoubleOrNull() }.maxOrNull() ?: 0.0
            } ?: 1000.0

            chartData = AccountBalanceChartData(accounts, history, dataMax)
        } catch (e: Exception) {
            error = "Failed to fetch data: ${e.message}"
        } finally {
            onLoadingStatusChange(false)
        }
    }

    chartData?.let { data ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .then(fixedChartHeight)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Account Balance History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ChartCanvas(data = data, modifier = Modifier.weight(1f))
        }
    }

    error?.let {
        Text(it, color = Color.Red, modifier = Modifier.padding(16.dp))
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ChartCanvas(data: AccountBalanceChartData, modifier: Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val accounts = data.accounts
    val history = data.history
    val roundedMax = (ceil(data.dataMax / 1000.0) * 1000.0).coerceAtLeast(1000.0)

    val sortedDates = remember(history) {
        history.map { LocalDate.parse(it.name) }.sorted()
    }

    val dataSeries = remember(accounts, history, sortedDates) {
        accounts.associate { account ->
            account.name to sortedDates.map { date ->
                history.find { it.name == date.toString() }?.balances?.get(account.name)?.toFloatOrNull()
            }
        }
    }

    val colors = listOf(
        Color(0xFF1DB954), Color(0xFF6200EE), Color(0xFFFF5722),
        Color(0xFF03A9F4), Color(0xFFFFC107), Color(0xFF795548), Color(0xFF009688)
    )

    var tooltipData by remember { mutableStateOf<Pair<LocalDate, Map<String, Float>>?>(null) }
    var tooltipOffset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 72.dp, start = 80.dp, end = 32.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (sortedDates.size > 1) {
                            val stepX = size.width / (sortedDates.size - 1)
                            val index = (offset.x / stepX).toInt().coerceIn(0, sortedDates.lastIndex)
                            tooltipData = sortedDates[index] to dataSeries.mapValues { it.value[index] ?: 0f }
                            tooltipOffset = offset
                        }
                    }
                }
        ) {
            if (sortedDates.size < 2) return@Canvas

            val stepX = size.width / (sortedDates.size - 1)
            val yScale = size.height / roundedMax.toFloat()

            repeat(6) { i ->
                val y = i * size.height / 5
                drawLine(Color.LightGray, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                val label = "$${(roundedMax - (i * roundedMax / 5)).toInt()}"
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    topLeft = Offset(-70f, y - 10f),
                    style = TextStyle(color = Color.Gray, fontSize = 10.sp)
                )
            }

            sortedDates.forEachIndexed { i, date ->
                if (i % 2 == 0) {
                    val x = i * stepX
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${date.month.name.take(3)} ${date.dayOfMonth}",
                        topLeft = Offset(x - 20f, size.height + 20f),
                        style = TextStyle(color = Color.DarkGray, fontSize = 10.sp)
                    )
                }
            }

            dataSeries.entries.forEachIndexed { index, (_, balances) ->
                val color = colors[index % colors.size]
                val path = Path()
                var started = false

                balances.forEachIndexed { i, value ->
                    if (value != null) {
                        val x = i * stepX
                        val y = size.height - (value * yScale)
                        if (!started) {
                            path.moveTo(x, y)
                            started = true
                        } else {
                            path.lineTo(x, y)
                        }
                        drawCircle(color, radius = 6f, center = Offset(x, y))
                    }
                }
                drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            accounts.forEachIndexed { index, account ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Box(Modifier.size(12.dp).background(colors[index % colors.size], MaterialTheme.shapes.small))
                    Spacer(Modifier.width(4.dp))
                    Text(account.name, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        tooltipData?.let { (date, values) ->
            Surface(
                modifier = Modifier
                    .offset(x = (tooltipOffset.x / 2).dp, y = (tooltipOffset.y / 2).dp),
                shadowElevation = 8.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${date.month.name} ${date.dayOfMonth}, ${date.year}", style = MaterialTheme.typography.labelLarge)
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    values.forEach { (name, amount) ->
                        val color = colors[accounts.indexOfFirst { it.name == name }.coerceAtLeast(0) % colors.size]
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(color))
                            Spacer(Modifier.width(8.dp))
                            Text("$name: $${"%.2f".format(amount.toDouble())}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}