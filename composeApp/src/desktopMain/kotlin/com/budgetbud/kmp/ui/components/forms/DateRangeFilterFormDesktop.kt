package com.budgetbud.kmp.ui.components.forms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
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
    var expanded by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Showing results for $startDate - $endDate",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    modifier = Modifier
                        .size(30.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Filter by Date Range",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showStartPicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start: $startDate")
                        }

                        OutlinedButton(
                            onClick = { showEndPicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("End: $endDate")
                        }
                    }
                }
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