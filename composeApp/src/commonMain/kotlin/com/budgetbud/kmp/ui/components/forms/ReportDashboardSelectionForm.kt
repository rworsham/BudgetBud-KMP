package com.budgetbud.kmp.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.ReportDashboardEditData
import com.budgetbud.kmp.ui.components.AlertHandler
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ReportDashboardSelectionForm(
    apiClient: ApiClient,
    onSuccess: () -> Unit
) {
    var selectedReportId by remember { mutableStateOf("") }
    var xSize by remember { mutableStateOf("") }
    var ySize by remember { mutableStateOf("") }
    var reports by remember { mutableStateOf<List<ReportDashboardEditData>>(emptyList()) }

    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response: HttpResponse = apiClient.client.get("/user/dashboard-report-options/")
            reports = response.body()
        } catch (e: Exception) {
            error = "Failed to fetch reports"
        } finally {
            isLoading = false
        }
    }

    fun validateForm(): Boolean {
        return if (selectedReportId.isBlank() || xSize.isBlank() || ySize.isBlank()) {
            error = "Please fill in all required fields."
            false
        } else {
            true
        }
    }

    fun submitForm() {
        if (!validateForm()) return

        isSubmitting = true
        error = null

        coroutineScope.launch(Dispatchers.IO) {
            try {
                apiClient.client.post("/user/reports/") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "report" to selectedReportId,
                            "x_size" to xSize,
                            "y_size" to ySize
                        )
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                error = "Failed to add Report to Dashboard. Please try again."
            } finally {
                isSubmitting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DropdownSelector(
            label = "Select Report",
            options = reports.map { it.id to it.display_name },
            selectedOption = selectedReportId,
            onOptionSelected = { selectedReportId = it }
        )

        DropdownSelector(
            label = "Select X Size",
            options = listOf("33" to "Small", "66" to "Medium", "100" to "Large"),
            selectedOption = xSize,
            onOptionSelected = { xSize = it }
        )

        DropdownSelector(
            label = "Select Y Size",
            options = listOf("33" to "Small", "66" to "Medium", "100" to "Large"),
            selectedOption = ySize,
            onOptionSelected = { ySize = it }
        )

        Button(
            onClick = { submitForm() },
            enabled = !isLoading && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Submitting..." else "Submit")
        }

        error?.let {
            AlertHandler(alertMessage = it)
        }
    }
}
