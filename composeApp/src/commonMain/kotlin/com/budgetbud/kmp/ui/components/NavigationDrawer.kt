package com.budgetbud.kmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MiniDrawer(
    expanded: Boolean,
    currentSegment: String,
    onSegmentSelected: (String) -> Unit,
    onToggle: () -> Unit
) {
    val drawerItems = listOf(
        DrawerItem.Segment("dashboard", "Dashboard", Icons.Default.Dashboard),
        DrawerItem.Segment("budget", "Budget", Icons.Default.AccountBalance),
        DrawerItem.Segment("transactions", "Transactions", Icons.Default.Receipt),
        DrawerItem.Divider,
        DrawerItem.Segment("category", "Categories", Icons.Default.Category),
        DrawerItem.Segment("accounts", "Accounts", Icons.Default.AccountBox),
        DrawerItem.Segment("family", "Family", Icons.Default.Group),
        DrawerItem.Segment("reports", "Reports", Icons.Default.BarChart)
    )

    val drawerWidth = if (expanded) 155.dp else 55.dp

    Surface(
        modifier = Modifier
            .width(drawerWidth)
            .fillMaxHeight()
    ) {
        Column {
            Spacer(Modifier.height(8.dp))

            drawerItems.forEach { item ->
                when (item) {
                    is DrawerItem.Divider -> HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    is DrawerItem.Header -> {
                        AnimatedVisibility(visible = expanded) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                    is DrawerItem.Segment -> {
                        val selected = currentSegment == item.segment
                        val backgroundColor = if (selected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else
                            MaterialTheme.colorScheme.surface

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor)
                                .clickable { onSegmentSelected(item.segment) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(start = 18.dp, top = 12.dp, bottom = 12.dp, end = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                AnimatedVisibility(visible = expanded) {
                                    Text(
                                        text = item.title,
                                        modifier = Modifier.padding(start = 16.dp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}