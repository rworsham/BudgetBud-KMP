package com.budgetbud.kmp.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
)