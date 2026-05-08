package com.budgetbud.kmp.ui.components.forms

import com.budgetbud.kmp.auth.ApiClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.ui.components.AlertHandler
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactForm(
    modifier: Modifier = Modifier,
    apiClient: ApiClient,
) {
    var email by remember { mutableStateOf("") }
    var inquiryType by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val inquiryOptions = listOf("General Inquiry", "Technical Support", "Feedback", "Other")
    val coroutineScope = rememberCoroutineScope()

    fun validateForm(): Boolean {
        if (email.isBlank()) {
            errorMessage = "Please provide your email address"
            return false
        }
        if (inquiryType.isBlank() || message.isBlank()) {
            errorMessage = "Please provide both a subject and a message"
            return false
        }
        return true
    }

    fun submitForm() {
        if (!validateForm()) return

        isSubmitting = true
        errorMessage = null

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val tokens = apiClient.getTokens()

                apiClient.client.post("https://api.budgetingbud.com/api/contact/") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        buildMap {
                            put("inquiryType", inquiryType)
                            put("message", message)
                            put("email", email)
                        }
                    )
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }


            } catch (e: Exception) {
                errorMessage = "Failed to send message. Please try again"
            } finally {
                isSubmitting = false
            }
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = inquiryType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Inquiry Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                inquiryOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            inquiryType = option
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )

        Button(
            onClick = { submitForm() },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Submitting..." else "Submit")
        }

        errorMessage?.let {
            AlertHandler(alertMessage = it)
        }
    }
}