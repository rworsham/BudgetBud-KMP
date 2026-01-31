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
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetbud.kmp.models.IncomeExpenseBarChartData
import kotlin.math.ceil

@OptIn(ExperimentalTextApi::class)
@Composable
actual fun IncomeExpenseBudgetBarChart(
    data: List<IncomeExpenseBarChartData>,
    modifier: Modifier
) {
    if (data.isEmpty()) {
        Text("No data available", modifier = modifier)
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
    val roundedMaxValue = (ceil(maxValue / 1000.0) * 1000.0).coerceAtLeast(1000.0)

    Column(modifier = modifier.fillMaxWidth()) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(horizontal = 8.dp)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val barCount = data.size
            val spaceBetween = chartWidth / barCount
            val barWidth = spaceBetween * 0.6f
            val heightScale = chartHeight / roundedMaxValue.toFloat()
            val numberOfGridLines = 5
            for (i in 0..numberOfGridLines) {
                val y = chartHeight - (i * chartHeight / numberOfGridLines)

                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f
                )

                val labelValue = (roundedMaxValue * i / numberOfGridLines)
                drawText(
                    textMeasurer = textMeasurer,
                    text = String.format("%,.0f", labelValue),
                    topLeft = Offset(4.dp.toPx(), y - 12.sp.toPx()),
                    style = TextStyle(
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            data.forEachIndexed { index, item ->
                val barHeight = (item.value * heightScale).toFloat()
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = if (item.name == "Income") Color(0xFF1DB954) else Color(0xFF8884d8),
                    topLeft = Offset(xOffset, chartHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )

                val labelResult = textMeasurer.measure(
                    text = AnnotatedString(item.name),
                    style = TextStyle(
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                )

                drawText(
                    textLayoutResult = labelResult,
                    topLeft = Offset(
                        x = xOffset + (barWidth - labelResult.size.width) / 2,
                        y = chartHeight + 8.dp.toPx()
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            data.forEach {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}