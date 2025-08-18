package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.ReportDashboardData
import com.budgetbud.kmp.ui.components.forms.ReportDashboardEditForm
import com.budgetbud.kmp.ui.components.forms.ReportDashboardSelectionForm
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

@Composable
fun ReportDashboard(
    familyView: Boolean,
    apiClient: ApiClient,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var userReports by remember { mutableStateOf<List<ReportDashboardData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var openDialog by remember { mutableStateOf(false) }
    var modalType by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    fun fetchReports() {
        coroutineScope.launch {
            isLoading = true
            try {
                val queryParams = listOf("familyView" to familyView.toString())

                val response = apiClient.client.get("https://api.budgetingbud.com/api/user/reports") {
                    url {
                        queryParams.forEach { (k, v) -> parameters.append(k, v) }
                    }
                }.body<List<ReportDashboardData>>()

                userReports = response
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch user reports"
            } finally {
                isLoading = false
            }
        }
    }

    fun handleOpen(type: String) {
        modalType = type
        openDialog = true
    }

    fun handleClose() {
        modalType = ""
        openDialog = false
        showSuccessDialog = false
    }

    fun handleFormSuccess() {
        showSuccessDialog = true
        coroutineScope.launch {
            delay(3000)
            handleClose()
            fetchReports()
        }
    }

    LaunchedEffect(familyView) {
        fetchReports()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Custom Reports",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Divider(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            OutlinedCard(onClick = { handleOpen("addReport") }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(Icons.Default.AddBox, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Report")
                }
            }
            OutlinedCard(onClick = { handleOpen("editReport") }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Reports")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (userReports.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                userReports.forEach { report ->
                    when (report.displayName) {
                        "Expense Categories Pie Chart" -> ExpenseCategoriesPieChart(report, familyView)
                        "Budget Vs Remaining Budget" -> BudgetRemainingBudgetBarChart(report, familyView)
                        "Expense Categories Bar Chart" -> ExpenseCategoriesBarChart(report, familyView)
                        "Account Balance History Line Chart" -> AccountBalanceHistoryLineChart(report, familyView)
                        "Income vs. Expense Bar Chart" -> IncomeExpenseBarChart(report, familyView)
                        "Category Expense Line Chart" -> CategoryExpenseLineChart(report, familyView)
                        "Family Contributions Bar Chart" -> FamilyContributionsBarChart(report, familyView)
                        "Family Category Usage Bar Chart" -> CategoryUsagePerUserBarChart(report, familyView)
                    }
                }
            }
        } else {
            Text("No reports available.")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (openDialog) {
            Dialog(onDismissRequest = { handleClose() }) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    when (modalType) {
                        "addReport" -> ReportDashboardSelectionForm(
                            apiClient = apiClient,
                            onSuccess = { handleFormSuccess() }
                        )
                        "editReport" -> ReportDashboardEditForm(
                            apiClient = apiClient,
                            onSuccess = { handleFormSuccess() }
                        )
                    }
                }
            }
        }

        if (showSuccessDialog) {
            SuccessDialog(onDismiss = { showSuccessDialog = false })
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        }

        errorMessage?.let {
            AlertHandler(alertMessage = it)
        }
    }
}
