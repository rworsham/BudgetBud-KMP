package com.budgetbud.kmp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun SuccessDialog(
    onDismiss: () -> Unit,
    message: String = "Operation completed successfully."
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = { Text("Success") },
        text = { Text(message) }
    )
}