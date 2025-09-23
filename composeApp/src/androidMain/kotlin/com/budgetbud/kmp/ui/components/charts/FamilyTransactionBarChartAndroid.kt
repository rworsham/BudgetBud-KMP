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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import com.budgetbud.kmp.models.FamilyTransactionOverviewData

@Composable
actual fun FamilyTransactionBarChart(
    data: List<FamilyTransactionOverviewData>,
    modifier: Modifier
) {
    val maxCount = data.maxOfOrNull { it.transaction_count } ?: 1f
    val roundedMax = ceil(maxCount / 10f) * 10f

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 16.dp)
        ) {
            val barWidth = size.width / data.size * 0.6f
            val spaceBetween = size.width / data.size
            val heightScale = size.height / roundedMax

            data.forEachIndexed { index, item ->
                val barHeight = item.transaction_count * heightScale
                val xOffset = index * spaceBetween + (spaceBetween - barWidth) / 2

                drawRect(
                    color = Color(0xFF8884D8),
                    topLeft = Offset(xOffset, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            data.forEach {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    maxLines = 1
                )
            }
        }
    }
}
