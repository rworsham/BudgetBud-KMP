package com.budgetbud.kmp.ui.components.charts

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.graphics.Paint
import android.graphics.Typeface
import com.budgetbud.kmp.models.IncomeExpenseBarChartData
import androidx.compose.ui.graphics.toArgb
import kotlin.math.ceil

@SuppressLint("DefaultLocale")
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
                .padding(horizontal = 4.dp)
        ) {
            val barWidth = (size.width / data.size * 0.6f).toDouble()
            val spaceBetween = (size.width / data.size).toDouble()
            val heightScale = size.height / roundedMaxValue
            val labelXOffset = 4.dp.toPx()

            val textPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 20f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }

            val numberOfGridLines = 5
            for (i in 1 until numberOfGridLines) {
                val y = size.height - (i * size.height / numberOfGridLines)

                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )

                val value = roundedMaxValue * (i / numberOfGridLines.toFloat())

                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%,.0f", value),
                    labelXOffset,
                    y - textPaint.descent(),
                    textPaint
                )
            }

            for (i in 1 until data.size) {
                val x = (i * spaceBetween).toFloat()
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            val xAxisPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 16f
                textAlign = Paint.Align.CENTER
            }

            data.forEachIndexed { index, item ->
                val barHeight = item.value * heightScale
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = if (item.name == "Income") Color.Green else Color.Red,
                    topLeft = Offset(xOffset.toFloat(), (size.height - barHeight).toFloat()),
                    size = Size(barWidth.toFloat(), barHeight.toFloat())
                )

                drawContext.canvas.nativeCanvas.drawText(
                    item.name,
                    xOffset.toFloat() + (barWidth.toFloat() / 2),
                    size.height + 16f,
                    xAxisPaint
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
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