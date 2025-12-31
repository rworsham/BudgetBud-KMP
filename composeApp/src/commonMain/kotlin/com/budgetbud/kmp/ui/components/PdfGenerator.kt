package com.budgetbud.kmp.ui.components

import androidx.compose.runtime.Composable

interface PdfCanvas {
    fun drawText(text: String, x: Float, y: Float, fontSize: Float)
}

@Composable
expect fun rememberPdfGenerator(
    fileName: String,
    onResult: (Boolean, String) -> Unit,
    onDraw: (PdfCanvas) -> Unit
): () -> Unit