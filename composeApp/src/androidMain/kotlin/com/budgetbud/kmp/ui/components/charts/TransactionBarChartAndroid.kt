package com.budgetbud.kmp.ui.components.charts

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.TransactionBarChartData
import kotlin.math.ceil

@SuppressLint("DefaultLocale")
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
            .padding(horizontal = 4.dp)
        ) {
            val barWidth = (size.width / parsedData.size * 0.6f)
            val spaceBetween = (size.width / parsedData.size)
            val heightScale = size.height / roundedMax
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

                val value = roundedMax * (i / numberOfGridLines.toFloat())

                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%,.0f", value),
                    labelXOffset,
                    y - textPaint.descent(),
                    textPaint
                )
            }

            for (i in 1 until parsedData.size) {
                val x = (i * spaceBetween)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            val xAxisPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 30f
                textAlign = Paint.Align.CENTER
            }

            parsedData.forEachIndexed { index, (item, amount) ->
                val barHeight = amount * heightScale
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = Color(0xFF1DB954),
                    topLeft = Offset(
                        x = xOffset,
                        y = size.height - barHeight
                    ),
                    size = Size(
                        width = barWidth,
                        height = barHeight
                    )
                )

                drawContext.canvas.nativeCanvas.drawText(
                    item.category,
                    xOffset + (barWidth / 2),
                    size.height + 24f,
                    xAxisPaint
                )
            }
        }
    }
}