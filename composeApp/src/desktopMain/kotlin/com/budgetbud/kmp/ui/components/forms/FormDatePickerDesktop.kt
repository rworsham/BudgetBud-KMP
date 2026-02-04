package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDate as KxLocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun FormDatePicker(
    label: String,
    selectedDate: KxLocalDate?,
    onDateSelected: (KxLocalDate) -> Unit,
    modifier: Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            val buttonText = selectedDate?.toString() ?: "Select $label"
            Text(buttonText)
        }
    }

    if (showDialog) {
        val initialDateMillis = remember(selectedDate) {
            selectedDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
                ?: Clock.System.now().toEpochMilliseconds()
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )



        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC).date
                            onDateSelected(date)
                        }
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Select $label",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}