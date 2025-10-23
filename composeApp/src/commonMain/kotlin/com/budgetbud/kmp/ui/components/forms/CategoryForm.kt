package com.budgetbud.kmp.ui.components.forms

import com.budgetbud.kmp.auth.ApiClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.CategoryData
import com.budgetbud.kmp.ui.components.AlertHandler
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CategoryForm(
    modifier: Modifier = Modifier,
    apiClient: ApiClient,
    onSuccess: () -> Unit,
    familyView: Boolean = false
) {
    var newCategory by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var existingCategories by remember { mutableStateOf<List<String>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val tokens = apiClient.getTokens()
            val response = apiClient.client.get("https://api.budgetingbud.com/api/categories/") {
                headers {
                    tokens?.let {
                        append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                    }
                }
            }
            val categories = response.body<List<CategoryData>>()
            existingCategories = categories.map { it.name }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to fetch existing categories"
        } finally {
            isLoading = false
        }
    }

    fun validateForm(): Boolean {
        if (newCategory.isBlank()) {
            errorMessage = "Please fill in all required fields"
            return false
        }

        if (existingCategories.contains(newCategory)) {
            errorMessage = "Category name already exists"
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

                apiClient.client.post("https://api.budgetingbud.com/api/categories/") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "name" to newCategory,
                        )
                    )
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }

                onSuccess()

            } catch (e: Exception) {
                errorMessage = "Failed to create new budget"
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
            value = newCategory,
            onValueChange = { newCategory = it },
            label = { Text("New Category") },
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
