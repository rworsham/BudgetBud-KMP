package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.vanpra.composematerialdialogs.*
import com.vanpra.composematerialdialogs.datetime.date.datepicker
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
        buttons = {
            positiveButton("OK")
            negativeButton("Cancel")
        }
    ) {
        datepicker(
            initialDate = selectedDate?.toJavaLocalDate() ?: JavaLocalDate.now(),
            title = "Select $label"
        ) { selected ->
            onDateSelected(selected.toKotlinLocalDate())
        }
    }
}