package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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
import com.budgetbud.kmp.models.TransactionBarChartData
import kotlin.math.ceil

@OptIn(ExperimentalTextApi::class)
@Composable
actual fun TransactionBarChart(
    data: List<TransactionBarChartData>,
    modifier: Modifier
) {
    if (data.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()

    val parsedData = data.map {
        val amount = it.total_amount.toFloatOrNull() ?: 0f
        it to amount
    }

    val maxAmount = parsedData.maxOfOrNull { it.second } ?: 1f
    val roundedMax = (ceil(maxAmount / 1000f) * 1000f).coerceAtLeast(1000f)

    Column(modifier = modifier.fillMaxWidth()) {


        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(top = 16.dp, bottom = 48.dp, start = 8.dp, end = 8.dp)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val barCount = parsedData.size
            val spaceBetween = chartWidth / barCount
            val barWidth = spaceBetween * 0.6f
            val heightScale = chartHeight / roundedMax

            val numberOfGridLines = 5
            for (i in 1..numberOfGridLines) {
                val y = chartHeight - (i * chartHeight / numberOfGridLines)

                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f
                )

                val value = roundedMax * (i / numberOfGridLines.toFloat())
                drawText(
                    textMeasurer = textMeasurer,
                    text = String.format("%,.0f", value),
                    topLeft = Offset(4.dp.toPx(), y - 10.sp.toPx()),
                    style = TextStyle(
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            for (i in 1 until barCount) {
                val x = i * spaceBetween
                drawLine(
                    color = Color.Gray.copy(alpha = 0.1f),
                    start = Offset(x, 0f),
                    end = Offset(x, chartHeight),
                    strokeWidth = 1f
                )
            }

            parsedData.forEachIndexed { index, (item, amount) ->
                val barHeight = amount * heightScale
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = Color(0xFF1DB954),
                    topLeft = Offset(xOffset, chartHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )

                val labelResult = textMeasurer.measure(
                    text = AnnotatedString(item.category),
                    style = TextStyle(
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                )

                drawText(
                    textLayoutResult = labelResult,
                    topLeft = Offset(
                        x = xOffset + (barWidth - labelResult.size.width) / 2,
                        y = chartHeight + 12.dp.toPx()
                    )
                )
            }
        }
    }
}