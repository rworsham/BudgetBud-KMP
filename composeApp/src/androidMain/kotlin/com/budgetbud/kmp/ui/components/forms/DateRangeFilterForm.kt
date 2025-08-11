package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vanpra.composematerialdialogs.*
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.budgetbud.kmp.utils.toJavaLocalDate
import com.budgetbud.kmp.utils.toKotlinLocalDate
import kotlinx.datetime.LocalDate as KxLocalDate
import java.time.LocalDate as JavaLocalDate

@Composable
actual fun DateRangeFilterForm(
    startDate: KxLocalDate,
    endDate: KxLocalDate,
    onStartDateChange: (KxLocalDate) -> Unit,
    onEndDateChange: (KxLocalDate) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier
) {
    val startDialogState = rememberMaterialDialogState()
    val endDialogState = rememberMaterialDialogState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply")
            }
        }
    }

    MaterialDialog(
        dialogState = startDialogState,
        buttons = {
            positiveButton("OK")
            negativeButton("Cancel")
        }
    ) {
        datepicker(
            initialDate = startDate.toJavaLocalDate(),
            title = "Select Start Date",
            allowedDateValidator = { it <= endDate.toJavaLocalDate() }
        ) { date: JavaLocalDate ->
            onStartDateChange(date.toKotlinLocalDate())
        }
    }

    MaterialDialog(
        dialogState = endDialogState,
        buttons = {
            positiveButton("OK")
            negativeButton("Cancel")
        }
    ) {
        datepicker(
            initialDate = endDate.toJavaLocalDate(),
            title = "Select End Date",
            allowedDateValidator = { it >= startDate.toJavaLocalDate() }
        ) { date: JavaLocalDate ->
            onEndDateChange(date.toKotlinLocalDate())
        }
    }
}
