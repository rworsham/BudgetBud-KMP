package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.FamilyData
import com.budgetbud.kmp.ui.components.AlertHandler
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FamilyCreateForm(
    modifier: Modifier = Modifier,
    apiClient: ApiClient,
    onSuccess: () -> Unit,
) {
    var newFamily by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var existingFamilies by remember { mutableStateOf<List<FamilyData>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response: HttpResponse = apiClient.client.get("https://api.budgetingbud.com/api/family/")
            existingFamilies = response.body()
        } catch (e: Exception) {
            errorMessage = "Failed to fetch family groups"
        } finally {
            isLoading = false
        }
    }

    fun validateForm(): Boolean {
        if (newFamily.isBlank()) {
            errorMessage = "Please fill in all required fields"
            return false
        }

        if (existingFamilies.isNotEmpty()) {
            errorMessage = "You are already a member of an existing family"
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

                apiClient.client.post("https://api.budgetingbud.com/api/family/create/") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf("name" to newFamily)
                    )
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to create Family group. Please try again."
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
            value = newFamily,
            onValueChange = { newFamily = it },
            label = { Text("New Family Group") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { submitForm() },
            enabled = !isLoading && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Submitting..." else "Submit")
        }

        errorMessage?.let {
            AlertHandler(alertMessage = it)
        }
    }
}
