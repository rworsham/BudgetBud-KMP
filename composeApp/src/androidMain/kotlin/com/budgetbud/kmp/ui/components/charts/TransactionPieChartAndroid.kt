package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.TransactionPieChartData

@Composable
actual fun TransactionPieChart(
    data: List<TransactionPieChartData>,
    modifier: Modifier
) {
    if (data.isEmpty()) return

    val parsedData = data.map {
        val amount = it.value.toFloatOrNull() ?: 0f
        it to amount
    }.filter { it.second > 0f }

    val totalAmount = parsedData.sumOf { it.second.toDouble() }.toFloat().takeIf { it > 0f } ?: return

    val colors = listOf(
        Color(0xFF1DB954),
        Color(0xFF3E9E52),
        Color(0xFF70B37E),
        Color(0xFF9CCC99),
        Color(0xFFCCE5CC)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .size(250.dp)
                .padding(16.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .wrapContentWidth()
        ) {
            parsedData.forEachIndexed { index, (item, amount) ->
                val percent = (amount / totalAmount) * 100
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colors[index % colors.size], shape = MaterialTheme.shapes.small)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.name}: ${"%.1f".format(percent)}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}