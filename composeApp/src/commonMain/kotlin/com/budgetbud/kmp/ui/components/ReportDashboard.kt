package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.ReportDashboardData
import com.budgetbud.kmp.ui.components.charts.*
import com.budgetbud.kmp.ui.components.forms.ReportDashboardEditForm
import com.budgetbud.kmp.ui.components.forms.ReportDashboardSelectionForm
import com.budgetbud.kmp.utils.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders

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

    fun fetchReports(isRetry: Boolean = false) {
        coroutineScope.launch {
            isLoading = true
            try {
                val queryParams = listOf("familyView" to familyView.toString())
                val tokens = apiClient.getTokens()

                val response = apiClient.client.get("https://api.budgetingbud.com/api/user/reports/") {
                    url {
                        queryParams.forEach { (k, v) -> parameters.append(k, v) }
                    }
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }

                userReports = response.body<List<ReportDashboardData>>()
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

    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Custom Reports",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(16.dp))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            thickness = 2.dp
        )

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { handleOpen("addReport") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Add Report",
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Button(
                onClick = { handleOpen("editReport") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Edit Report",
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (userReports.isNotEmpty()) {
            val startDate = DateUtils.firstDayOfCurrentMonth().toString()
            val endDate = DateUtils.lastDayOfCurrentMonth().toString()

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                userReports.forEach { report ->
                    when (report.display_name) {
                        "Expense Categories Pie Chart" -> ExpenseCategoriesPieChart(
                            startDate = startDate,
                            endDate = endDate,
                            familyView = familyView,
                            modifier = Modifier,
                            apiClient = apiClient
                        )
                        "Budget Vs Remaining Budget" -> BudgetRemainingBudgetBarChart(
                            startDate = startDate,
                            endDate = endDate,
                            familyView = familyView,
                            modifier = Modifier,
                            apiClient = apiClient
                        )
                        "Expense Categories Bar Chart" -> ExpenseCategoriesBarChart(
                            startDate = startDate,
                            endDate = endDate,
                            familyView = familyView,
                            modifier = Modifier,
                            apiClient = apiClient
                        )
                        "Account Balance History Line Chart" -> AccountBalanceHistoryLineChart(
                            familyView = familyView,
                            modifier = Modifier,
                            apiClient = apiClient
                        )
                        "Income vs. Expense Bar Chart" -> IncomeExpenseBarChart(
                            startDate = startDate,
                            endDate = endDate,
                            familyView = familyView,
                            modifier = Modifier,
                            apiClient = apiClient
                        )
                        "Category Expense Line Chart" -> CategoryExpenseLineChart(
                            familyView = familyView,
                            modifier = Modifier,
                            apiClient = apiClient
                        )
                        "Family Contributions Bar Chart" -> FamilyContributionsBarChart(
                            startDate = startDate,
                            endDate = endDate,
                            familyView = familyView,
                            modifier = Modifier,
                            apiClient = apiClient
                        )
                        "Family Category Usage Bar Chart" -> CategoryUsagePerUserBarChart(
                            startDate = startDate,
                            endDate = endDate,
                            familyView = familyView,
                            modifier = Modifier,
                            apiClient = apiClient
                        )
                    }
                }
            }
        } else {
            ChartDataError("No Reports Added / Available")
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator(modifier = Modifier
                    .padding(16.dp)
                    .size(72.dp))
            }
        }

        errorMessage?.let {
            AlertHandler(alertMessage = it)
        }
    }
}
