package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDate as KxLocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun DateRangeFilterForm(
    startDate: KxLocalDate,
    endDate: KxLocalDate,
    onStartDateChange: (KxLocalDate) -> Unit,
    onEndDateChange: (KxLocalDate) -> Unit,
    modifier: Modifier
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedButton(
                onClick = { showStartPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Start Date: $startDate",
                    maxLines = 1,
                    style = TextStyle(fontSize = 14.sp)
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showEndPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "End Date: $endDate",
                    maxLines = 1,
                    style = TextStyle(fontSize = 14.sp)
                )
            }
        }
    }

    if (showStartPicker) {
        DesktopDatePickerDialog(
            initialDate = startDate,
            title = "Select Start Date",
            selectableDates = { dateMillis ->
                val endMillis = endDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
                dateMillis <= endMillis
            },
            onDateSelected = { date ->
                if (date != null) onStartDateChange(date)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        DesktopDatePickerDialog(
            initialDate = endDate,
            title = "Select End Date",
            selectableDates = { dateMillis ->
                val startMillis = startDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
                dateMillis >= startMillis
            },
            onDateSelected = { date ->
                if (date != null) onEndDateChange(date)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopDatePickerDialog(
    initialDate: KxLocalDate,
    title: String,
    selectableDates: (Long) -> Boolean,
    onDateSelected: (KxLocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return selectableDates(utcTimeMillis)
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.fromEpochMilliseconds(it)
                            .toLocalDateTime(TimeZone.UTC).date
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        )
    }
}