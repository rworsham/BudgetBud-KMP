package com.budgetbud.kmp.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

sealed class DrawerItem {
    data class Segment(val segment: String, val title: String, val icon: ImageVector) : DrawerItem()
    data class Header(val title: String) : DrawerItem()
    object Divider : DrawerItem()
}