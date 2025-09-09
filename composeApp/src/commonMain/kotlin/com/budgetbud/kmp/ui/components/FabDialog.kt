package com.budgetbud.kmp.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun FabDialog(
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
)