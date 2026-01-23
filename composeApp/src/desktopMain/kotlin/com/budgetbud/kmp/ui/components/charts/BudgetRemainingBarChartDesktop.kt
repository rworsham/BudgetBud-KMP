package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetbud.kmp.models.BudgetRemainingBudgetBarChartData
import kotlin.math.ceil

@Composable
actual fun BudgetRemainingBarChart(
    data: List<BudgetRemainingBudgetBarChartData>,
    modifier: Modifier
) {
    if (data.isEmpty()) {
        Text("No data available", modifier = modifier.padding(16.dp))
    } else {
        DrawChart(data = data, modifier = modifier)
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun DrawChart(data: List<BudgetRemainingBudgetBarChartData>, modifier: Modifier) {
    val textMeasurer = rememberTextMeasurer()

    val maxBudget = data.flatMap { listOf(it.starting_budget, it.remaining_budget) }.maxOrNull() ?: 1.0
    val roundedMax = (ceil(maxBudget / 1000.0) * 1000.0).coerceAtLeast(1000.0)

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
        ) {
            val barGroupWidth = size.width / data.size
            val heightScale = size.height / roundedMax.toFloat()
            val singleBarWidth = barGroupWidth / 5.0f
            val labelXOffset = 4.dp.toPx()

            val numberOfGridLines = 5
            for (i in 0..numberOfGridLines) {
                val y = size.height - (i * size.height / numberOfGridLines)

                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )

                val value = roundedMax * (i / numberOfGridLines.toDouble())
                drawText(
                    textMeasurer = textMeasurer,
                    text = String.format("%,.0f", value),
                    topLeft = Offset(labelXOffset, y - 10.dp.toPx()),
                    style = TextStyle(
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            for (i in 1 until data.size) {
                val x = i * barGroupWidth
                drawLine(
                    color = Color.Gray.copy(alpha = 0.1f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            data.forEachIndexed { index, item ->
                val startHeight = (item.starting_budget * heightScale).toFloat()
                val remainingHeight = (item.remaining_budget * heightScale).toFloat()

                val barX = index * barGroupWidth
                val totalBarPairWidth = 2 * singleBarWidth
                val centeringSpace = (barGroupWidth - totalBarPairWidth) / 2
                val startX = barX + centeringSpace

                drawRect(
                    color = Color(0xFF8884d8),
                    topLeft = Offset(startX, size.height - startHeight),
                    size = Size(singleBarWidth, startHeight)
                )

                drawRect(
                    color = Color(0xFF82ca9d),
                    topLeft = Offset(startX + singleBarWidth, size.height - remainingHeight),
                    size = Size(singleBarWidth, remainingHeight)
                )

                val textLayoutResult = textMeasurer.measure(
                    text = AnnotatedString(item.name),
                    style = TextStyle(color = Color.Gray, fontSize = 11.sp)
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = barX + (barGroupWidth - textLayoutResult.size.width) / 2,
                        y = size.height + 8.dp.toPx()
                    )
                )
            }
        }
    }
}