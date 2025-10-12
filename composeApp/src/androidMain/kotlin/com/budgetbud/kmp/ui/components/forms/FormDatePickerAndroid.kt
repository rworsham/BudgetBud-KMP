package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.budgetbud.kmp.utils.toJavaLocalDate
import com.budgetbud.kmp.utils.toKotlinLocalDate
import kotlinx.datetime.LocalDate as KxLocalDate
import java.time.LocalDate as JavaLocalDate

@Composable
actual fun FormDatePicker(
    label: String,
    selectedDate: KxLocalDate?,
    onDateSelected: (KxLocalDate) -> Unit,
    modifier: Modifier
) {
    val dialogState = rememberMaterialDialogState()

    Column(modifier = modifier) {
        OutlinedButton(
            onClick = { dialogState.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            val buttonText = selectedDate?.toString() ?: "Select $label"
            Text(buttonText)
        }
    }

    MaterialDialog(
        dialogState = dialogState,
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
            initialDate = selectedDate?.toJavaLocalDate() ?: JavaLocalDate.now(),
            title = "Select $label",
            colors = DatePickerDefaults.colors(
                headerBackgroundColor = MaterialTheme.colorScheme.primary,
                headerTextColor = MaterialTheme.colorScheme.onPrimary,
                calendarHeaderTextColor = MaterialTheme.colorScheme.onSurface,
                dateActiveBackgroundColor = MaterialTheme.colorScheme.primary,
                dateActiveTextColor = MaterialTheme.colorScheme.onPrimary,
                dateInactiveTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        ) { selected ->
            onDateSelected(selected.toKotlinLocalDate())
        }
    }
}