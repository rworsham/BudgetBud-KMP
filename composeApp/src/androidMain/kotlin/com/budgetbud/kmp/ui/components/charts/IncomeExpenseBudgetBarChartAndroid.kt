package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.IncomeExpenseBarChartData
import kotlin.math.ceil

@Composable
actual fun IncomeExpenseBudgetBarChart(
    data: List<IncomeExpenseBarChartData>,
    modifier: Modifier
) {
    if (data.isEmpty()) {
        Text("No data available", modifier = modifier)
        return
    }

    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
    val roundedMaxValue = ceil(maxValue / 1000.0) * 1000.0

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 16.dp)
        ) {
            val barWidth = (size.width / data.size * 0.6f).toDouble()
            val spaceBetween = (size.width / data.size).toDouble()
            val heightScale = size.height / roundedMaxValue

            data.forEachIndexed { index, item ->
                val barHeight = item.value * heightScale
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = if (item.name == "Income") Color.Green else Color.Red,
                    topLeft = Offset(xOffset.toFloat(), (size.height - barHeight).toFloat()),
                    size = Size(barWidth.toFloat(), barHeight.toFloat())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            data.forEach {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
        }
    }
}
