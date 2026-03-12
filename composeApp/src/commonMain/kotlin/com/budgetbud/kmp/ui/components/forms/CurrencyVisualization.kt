package com.budgetbud.kmp.ui.components.forms

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class CurrencyAmountInputVisualTransformation : VisualTransformation {
    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    private val formatter = DecimalFormat("#,###.##", symbols)

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val parts = originalText.split(".")
        val intPart = parts[0].toLongOrNull()?.let { formatter.format(it) } ?: parts[0]
        val decPart = if (parts.size > 1) "." + parts[1] else if (originalText.endsWith(".")) "." else ""

        val formattedText = intPart + decPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val originalSub = originalText.take(offset)
                val partsSub = originalSub.split(".")
                val intSub = partsSub[0].toLongOrNull()?.let { formatter.format(it) } ?: partsSub[0]
                val decSub = if (partsSub.size > 1) "." + partsSub[1] else if (originalSub.endsWith(".")) "." else ""
                return (intSub + decSub).length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return formattedText.take(offset).replace(",", "").length
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}