package com.budgetbud.kmp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.budgetbud.kmp.ui.components.GradientTitle
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
        undecorated = true,
        state = windowState
    ) {
        window.minimumSize = Dimension(1000, 800)

        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {

            WindowDraggableArea {
                Surface(
                    color = Color(0xFF121212),
                    modifier = Modifier.fillMaxWidth().height(45.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                            GradientTitle(text = "Budget Bud", fontSize = 24.sp)
                        }

                        Row(modifier = Modifier.fillMaxHeight()) {

                            WindowControlButton(icon = Icons.Default.Remove) {
                                window.extendedState = java.awt.Frame.ICONIFIED
                            }

                            val isMaximized = window.extendedState == java.awt.Frame.MAXIMIZED_BOTH
                            WindowControlButton(
                                icon = if (isMaximized) Icons.Default.FilterNone else Icons.Default.CheckBoxOutlineBlank
                            ) {
                                window.extendedState = if (isMaximized) {
                                    java.awt.Frame.NORMAL
                                } else {
                                    java.awt.Frame.MAXIMIZED_BOTH
                                }
                            }

                            WindowControlButton(icon = Icons.Default.Close, isClose = true) {
                                exitApplication()
                            }
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color.White.copy(alpha = 0.1f))

            Box(modifier = Modifier.fillMaxSize()) {
                App()
            }
        }
    }
}

@Composable
fun WindowControlButton(
    icon: ImageVector,
    isClose: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = when {
        isHovered && isClose -> Color(0xFFC42B1C)
        isHovered -> Color.White.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(45.dp)
            .background(backgroundColor)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color.White
        )
    }
}