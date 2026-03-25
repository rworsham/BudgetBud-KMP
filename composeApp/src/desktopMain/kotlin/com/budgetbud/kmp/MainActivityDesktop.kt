package com.budgetbud.kmp

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() = application {
    val windowState = rememberWindowState(
        width = 1400.dp,
        height = 900.dp,
        position = WindowPosition(Alignment.Center)
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "BudgetBud",
        state = windowState
    ) {
        window.minimumSize = Dimension(1000, 800)
        App()
    }
}