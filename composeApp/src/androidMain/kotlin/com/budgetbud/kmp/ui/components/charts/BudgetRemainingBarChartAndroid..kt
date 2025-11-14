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
import com.budgetbud.kmp.models.BudgetRemainingBudgetBarChartData
import kotlin.math.ceil

@Composable
actual fun BudgetRemainingBarChart(
    data: List<BudgetRemainingBudgetBarChartData>,
    modifier: Modifier
) {
    if (data.isEmpty()) {
        Text("No data available", modifier = modifier)
    } else {
        DrawChart(data = data, modifier = modifier)
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun DrawChart(data: List<BudgetRemainingBudgetBarChartData>, modifier: Modifier) {
    val maxBudget = data.flatMap { listOf(it.starting_budget, it.remaining_budget) }.maxOrNull() ?: 1.0
    val roundedMax = ceil(maxBudget / 1000.0) * 1000.0

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 4.dp)
        ) {
            val barGroupWidth = size.width / data.size
            val heightScale = if (roundedMax == 0.0) 0f else size.height / roundedMax.toFloat()
            val singleBarWidth = barGroupWidth / 5.0f
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

            for (i in 1 until data.size) {
                val x = (i * barGroupWidth)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            val xAxisPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 20f
                textAlign = Paint.Align.CENTER
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

                drawContext.canvas.nativeCanvas.drawText(
                    item.name,
                    (barX + barGroupWidth / 2),
                    size.height + 24f,
                    xAxisPaint
                )
            }
        }
    }
}