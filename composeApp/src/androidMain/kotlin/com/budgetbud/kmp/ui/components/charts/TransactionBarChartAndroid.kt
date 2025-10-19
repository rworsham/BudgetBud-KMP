package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.TransactionBarChartData
import kotlin.math.ceil

@Composable
actual fun TransactionBarChart(
    data: List<TransactionBarChartData>,
    modifier: Modifier
) {
    if (data.isEmpty()) return

    val parsedData = data.map {
        val amount = it.total_amount.toFloatOrNull() ?: 0f
        it to amount
    }

    val maxAmount = parsedData.maxOfOrNull { it.second } ?: 1f
    val roundedMax = ceil(maxAmount / 1000f) * 1000f

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 16.dp)
        ) {
            val barWidth = size.width / parsedData.size
            val heightScale = size.height / roundedMax

            parsedData.forEachIndexed { index, (item, amount) ->
                val barHeight = amount * heightScale
                drawRect(
                    color = Color(0xFF1DB954),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = index * barWidth + barWidth * 0.1f,
                        y = size.height - barHeight
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = barWidth * 0.8f,
                        height = barHeight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            parsedData.forEach { (item, _) ->
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
        }
    }
}
