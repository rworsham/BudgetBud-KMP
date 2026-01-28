package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.ExpenseCategoriesPieChartData

@Composable
actual fun ExpenseCategoriesBudgetPieChart(
    data: List<ExpenseCategoriesPieChartData>,
    modifier: Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data available", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        DrawTransactionPieChart(data, modifier)
    }
}

@Composable
private fun DrawTransactionPieChart(
    data: List<ExpenseCategoriesPieChartData>,
    modifier: Modifier = Modifier
) {
    val parsedData = data.map {
        val amount = it.value.toFloatOrNull() ?: 0f
        it to amount
    }.filter { it.second > 0f }

    val totalAmount = parsedData.sumOf { it.second.toDouble() }.toFloat()

    val colors = listOf(
        Color(0xFF1DB954),
        Color(0xFF3E9E52),
        Color(0xFF70B37E),
        Color(0xFF9CCC99),
        Color(0xFFCCE5CC)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (totalAmount <= 0f) return@Column



        Canvas(
            modifier = Modifier
                .size(300.dp)
                .padding(24.dp)
        ) {
            var startAngle = -90f

            parsedData.forEachIndexed { index, (_, amount) ->
                val sweepAngle = (amount / totalAmount) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 3
        ) {
            parsedData.forEachIndexed { index, (item, amount) ->
                val percent = (amount / totalAmount) * 100
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colors[index % colors.size], shape = MaterialTheme.shapes.extraSmall)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.name}: ${"%.1f".format(percent)}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}