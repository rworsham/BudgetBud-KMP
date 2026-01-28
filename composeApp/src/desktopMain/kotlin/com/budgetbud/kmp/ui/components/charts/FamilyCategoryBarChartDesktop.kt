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
import com.budgetbud.kmp.models.FamilyCategoryOverviewData
import kotlin.math.ceil

@OptIn(ExperimentalTextApi::class)
@Composable
actual fun FamilyCategoryBarChart(
    data: List<FamilyCategoryOverviewData>,
    modifier: Modifier
) {
    val textMeasurer = rememberTextMeasurer()
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
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(start = 50.dp, end = 20.dp, top = 20.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barCount = data.size
            val barSpacingPercent = 0.6f
            val barWidth = (canvasWidth / barCount) * barSpacingPercent
            val spaceBetweenBars = (canvasWidth / barCount) * (1 - barSpacingPercent)
            val heightScale = canvasHeight / roundedMax

            val numLines = 5
            for (i in 0..numLines) {
                val value = (roundedMax * i) / numLines
                val y = canvasHeight - (value * heightScale)

                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = value.toInt().toString(),
                    topLeft = Offset(-35.dp.toPx(), y - 10.dp.toPx()),
                    style = TextStyle(
                        color = onSurfaceColor,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End
                    )
                )
            }

            data.forEachIndexed { index, item ->
                val barHeight = item.category_count * heightScale
                val xStart = (index * (canvasWidth / barCount)) + (spaceBetweenBars / 2)

                drawRect(
                    color = primaryColor.copy(alpha = 0.8f),
                    topLeft = Offset(xStart, canvasHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )

                val categoryResult = textMeasurer.measure(
                    text = AnnotatedString(item.category),
                    style = TextStyle(
                        color = onSurfaceColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                drawText(
                    textLayoutResult = categoryResult,
                    topLeft = Offset(
                        x = xStart + (barWidth - categoryResult.size.width) / 2,
                        y = canvasHeight - barHeight - categoryResult.size.height - 4.dp.toPx()
                    )
                )
            }

            drawLine(
                color = onSurfaceColor,
                start = Offset(0f, canvasHeight),
                end = Offset(canvasWidth, canvasHeight),
                strokeWidth = 2.dp.toPx()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                        .weight(barSpaceWeight),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.weight(paddingWeight / 2))
            }
        }
    }
}