package com.example.budgetbud.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

@Composable
fun GradientTitle(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val brush = Brush.linearGradient(
        colors = listOf(Color(0xFF1DB954), Color(0xFF006400)),
        start = Offset(0f, 0f),
        end = Offset(200f, 200f)
    )

    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            brush = brush,
            textAlign = TextAlign.Center
        )
    )
}
