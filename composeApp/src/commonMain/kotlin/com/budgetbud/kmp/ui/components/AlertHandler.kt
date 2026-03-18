package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AlertHandler(
    alertMessage: String,
    modifier: Modifier = Modifier
) {
    var visible by remember(alertMessage) { mutableStateOf(true) }

    if (visible) {
        Snackbar(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White,
            action = {}
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = alertMessage,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { visible = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("DISMISS", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}