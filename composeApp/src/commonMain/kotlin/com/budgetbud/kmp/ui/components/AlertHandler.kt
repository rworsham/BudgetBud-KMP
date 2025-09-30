package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AlertHandler(
    alertMessage: String,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(true) }

    if (visible) {
        Snackbar(
            action = {
                TextButton(onClick = { visible = false }) {
                    Text("Dismiss", color = MaterialTheme.colorScheme.onError.copy(alpha = 1f))
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 1f),
            contentColor = MaterialTheme.colorScheme.onError.copy(alpha = 1f)
        ) {
            Text(alertMessage)
        }
    }
}
