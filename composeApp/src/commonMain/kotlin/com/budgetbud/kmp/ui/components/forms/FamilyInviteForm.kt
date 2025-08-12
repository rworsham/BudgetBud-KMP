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
fun FamilyInviteForm(
    modifier: Modifier = Modifier,
    apiClient: ApiClient,
    onSuccess: () -> Unit,
) {
    var newFamilyMember by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var existingFamily by remember { mutableStateOf<List<FamilyData>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response: HttpResponse = apiClient.client.get("https://api.budgetingbud.com/api/family/")
            existingFamily = response.body()
        } catch (e: Exception) {
            errorMessage = "Failed to fetch family information"
        } finally {
            isLoading = false
        }
    }

    fun validateForm(): Boolean {
        if (newFamilyMember.isBlank()) {
            errorMessage = "Please fill in all required fields"
            return false
        }
        if (existingFamily.isEmpty()) {
            errorMessage = "You must be a member of a family to invite others"
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
                apiClient.client.post("https://api.budgetingbud.com/api/family/invite/") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("invited_user" to newFamilyMember))
                }
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to invite member. Please try again."
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
            value = newFamilyMember,
            onValueChange = { newFamilyMember = it },
            label = { Text("New Family Member") },
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
