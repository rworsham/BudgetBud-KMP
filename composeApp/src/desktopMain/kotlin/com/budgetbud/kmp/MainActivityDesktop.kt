package com.budgetbud.kmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() = application {
    val windowState = rememberWindowState()
    Window(
        onCloseRequest = ::exitApplication,
        title = "BudgetBud",
        state = windowState
    ) {
        window.minimumSize = Dimension(1000, 800)
        App()
    }
}