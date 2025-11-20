package com.budgetbud.kmp.ui.components.charts

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.FamilyCategoryOverviewData
import kotlin.math.ceil

@Composable
actual fun FamilyCategoryBarChart(
    data: List<FamilyCategoryOverviewData>,
    modifier: Modifier
) {
    val maxCount = data.maxOfOrNull { it.category_count } ?: 1f
    val roundedMax = ceil(maxCount / 5f) * 5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Category Usage Per User",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(start = 40.dp, end = 16.dp, top = 8.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barCount = data.size
            val barSpacing = 0.6f
            val barWidth = (canvasWidth / barCount) * barSpacing
            val spaceBetweenBars = (canvasWidth / barCount) * (1 - barSpacing)
            val heightScale = canvasHeight / roundedMax
            val axisStrokeWidth = 1.dp.toPx()

            val yAxisTextPaint = Paint().apply {
                color = onSurfaceColor.toArgb()
                textSize = 20f
                textAlign = Paint.Align.RIGHT
                typeface = Typeface.DEFAULT
            }

            val barCategoryTextPaint = Paint().apply {
                color = onSurfaceColor.toArgb()
                textSize = 20f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }

            val numLines = 5
            for (i in 0..numLines) {
                val value = (roundedMax * i) / numLines
                val y = canvasHeight - (value * heightScale)

                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f
                )

                drawContext.canvas.nativeCanvas.drawText(
                    value.toInt().toString(),
                    -4.dp.toPx(),
                    y + (yAxisTextPaint.textSize / 3),
                    yAxisTextPaint
                )
            }

            data.forEachIndexed { index, item ->
                val barHeight = item.category_count * heightScale
                val xStart = (index * (barWidth + spaceBetweenBars)) + (spaceBetweenBars / 2)

                drawRect(
                    color = primaryColor.copy(alpha = 0.8f),
                    topLeft = Offset(xStart, canvasHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )

                val categoryText = item.category
                val textX = xStart + (barWidth / 2)

                val textY = canvasHeight - barHeight - 8.dp.toPx()

                drawContext.canvas.nativeCanvas.drawText(
                    categoryText,
                    textX,
                    textY,
                    barCategoryTextPaint
                )
            }

            drawLine(
                color = onSurfaceColor,
                start = Offset(0f, canvasHeight),
                end = Offset(canvasWidth, canvasHeight),
                strokeWidth = axisStrokeWidth
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            data.forEach { item ->
                val totalSpace = 1f / data.size.toFloat()
                val barSpaceWeight = totalSpace * 0.6f
                val paddingWeight = totalSpace * 0.4f

                Spacer(modifier = Modifier.weight(paddingWeight / 2))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(barSpaceWeight)
                        .padding(horizontal = 2.dp),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.weight(paddingWeight / 2))
            }
        }
    }
}
