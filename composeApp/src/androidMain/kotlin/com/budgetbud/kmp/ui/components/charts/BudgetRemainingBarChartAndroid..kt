package com.budgetbud.kmp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.budgetbud.kmp.models.BudgetRemainingBudgetBarChartData
import kotlin.math.ceil
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

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

@Composable
private fun DrawChart(data: List<BudgetRemainingBudgetBarChartData>, modifier: Modifier) {
    val maxBudget = data.flatMap { listOf(it.starting_budget, it.remaining_budget) }.maxOrNull() ?: 1.0
    val roundedMax = ceil(maxBudget / 1000.0) * 1000.0

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barGroupWidth = size.width / data.size
            val heightScale = if (roundedMax == 0.0) 0f else size.height / roundedMax.toFloat()

            data.forEachIndexed { index, item ->
                val startHeight = (item.starting_budget * heightScale).toFloat()
                val remainHeight = (item.remaining_budget * heightScale).toFloat()
                val barX = index * barGroupWidth
                val barWidth = barGroupWidth / 2.5f

                drawRect(
                    color = Color(0xFF8884d8),
                    topLeft = Offset(barX + barWidth * 0.25f, size.height - startHeight),
                    size = Size(barWidth, startHeight)
                )

                drawRect(
                    color = Color(0xFF82ca9d),
                    topLeft = Offset(barX + barWidth * 1.25f, size.height - remainHeight),
                    size = Size(barWidth, remainHeight)
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
