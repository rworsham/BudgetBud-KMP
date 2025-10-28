package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.budgetbud.kmp.utils.toJavaLocalDate
import com.budgetbud.kmp.utils.toKotlinLocalDate
import kotlinx.datetime.LocalDate as KxLocalDate

@Composable
actual fun DateRangeFilterForm(
    startDate: KxLocalDate,
    endDate: KxLocalDate,
    onStartDateChange: (KxLocalDate) -> Unit,
    onEndDateChange: (KxLocalDate) -> Unit,
    modifier: Modifier
) {
    val startDialogState = rememberMaterialDialogState()
    val endDialogState = rememberMaterialDialogState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedButton(
                onClick = { startDialogState.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Date: $startDate")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { endDialogState.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("End Date: $endDate")
            }
        }
    }

    MaterialDialog(
        dialogState = startDialogState,
        backgroundColor = MaterialTheme.colorScheme.surface,
        buttons = {
            positiveButton(
                text = "OK",
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
            negativeButton(
                text = "Cancel",
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
        }
    ) {
        datepicker(
            initialDate = startDate.toJavaLocalDate(),
            title = "Select Start Date",
            allowedDateValidator = { it <= endDate.toJavaLocalDate() },
            colors = DatePickerDefaults.colors(
                headerBackgroundColor = MaterialTheme.colorScheme.primary,
                headerTextColor = MaterialTheme.colorScheme.onPrimary,
                calendarHeaderTextColor = MaterialTheme.colorScheme.onSurface,
                dateActiveBackgroundColor = MaterialTheme.colorScheme.primary,
                dateActiveTextColor = MaterialTheme.colorScheme.onPrimary,
                dateInactiveTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        ) { date ->
            onStartDateChange(date.toKotlinLocalDate())
        }
    }

    MaterialDialog(
        dialogState = endDialogState,
        backgroundColor = MaterialTheme.colorScheme.surface,
        buttons = {
            positiveButton(
                text = "OK",
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
            negativeButton(
                text = "Cancel",
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
        }
    ) {
        datepicker(
            initialDate = endDate.toJavaLocalDate(),
            title = "Select End Date",
            allowedDateValidator = { it >= startDate.toJavaLocalDate() },
            colors = DatePickerDefaults.colors(
                headerBackgroundColor = MaterialTheme.colorScheme.primary,
                headerTextColor = MaterialTheme.colorScheme.onPrimary,
                calendarHeaderTextColor = MaterialTheme.colorScheme.onSurface,
                dateActiveBackgroundColor = MaterialTheme.colorScheme.primary,
                dateActiveTextColor = MaterialTheme.colorScheme.onPrimary,
                dateInactiveTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        ) { date ->
            onEndDateChange(date.toKotlinLocalDate())
        }
    }
}
