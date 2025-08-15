package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.ui.components.forms.DateRangeFilterForm
import com.budgetbud.kmp.ui.components.forms.BudgetForm
import com.budgetbud.kmp.ui.components.forms.BudgetEditForm
import com.budgetbud.kmp.ui.components.forms.BudgetGoalForm
import com.budgetbud.kmp.models.BudgetData
import com.budgetbud.kmp.models.BudgetReportData
import com.budgetbud.kmp.models.TransactionPieChartData
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*
import com.budgetbud.kmp.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
fun BudgetTransactionOverview(
    familyView: Boolean,
    apiClient: ApiClient,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var startDate by remember { mutableStateOf(DateUtils.firstDayOfCurrentMonth()) }
    var endDate by remember { mutableStateOf(DateUtils.lastDayOfCurrentMonth()) }

    var reportData by remember { mutableStateOf<BudgetReportData?>(null) }
    var pieChartData by remember { mutableStateOf<List<TransactionPieChartData>>(emptyList()) }

    var existingBudgets by remember { mutableStateOf<List<BudgetData>>(emptyList()) }
    var selectedBudgetId by remember { mutableStateOf<Int?>(null) }

    var openDialog by remember { mutableStateOf(false) }
    var modalType by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    fun handleOpen(type: String, budgetId: Int? = null) {
        modalType = type
        selectedBudgetId = budgetId
        openDialog = true
    }

    fun handleClose() {
        openDialog = false
        modalType = ""
        selectedBudgetId = null
        showSuccessDialog = false
    }

    fun handleFormSuccess() {
        showSuccessDialog = true
        coroutineScope.launch {
            kotlinx.coroutines.delay(5000)
            handleClose()
        }
    }

    fun fetchData() {
        coroutineScope.launch {
            if (!apiClient.isAuthenticated()) return@launch

            isLoading = true
            try {
                val queryParams = listOf("familyView" to familyView.toString())
                val datePayload = mapOf(
                    "start_date" to startDate.toString(),
                    "end_date" to endDate.toString()
                )

                val reportResponse = apiClient.client.post("https://api.budgetingbud.com/api/budget-transaction-overview/") {
                    contentType(ContentType.Application.Json)
                    setBody(datePayload)
                    url {
                        queryParams.forEach { (key, value) -> parameters.append(key, value) }
                    }
                }.body<BudgetReportData>()

                val pieResponse = apiClient.client.post("https://api.budgetingbud.com/api/transaction-pie-chart/") {
                    contentType(ContentType.Application.Json)
                    setBody(datePayload)
                    url {
                        queryParams.forEach { (key, value) -> parameters.append(key, value) }
                    }
                }.body<List<TransactionPieChartData>>()

                val budgetsResponse = apiClient.client.get("https://api.budgetingbud.com/api/budget/") {
                    url {
                        queryParams.forEach { (key, value) -> parameters.append(key, value) }
                    }
                }.body<List<BudgetData>>()

                reportData = reportResponse
                pieChartData = pieResponse
                existingBudgets = budgetsResponse
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error occurred"
            } finally {
                isLoading = false
            }
        }
    }


    val incomeExpenseData = remember(reportData) {
        listOf(
            ChartValue("Income", reportData?.transactions?.filter { it.transactionType == "income" }?.sumOf { it.amount } ?: 0.0),
            ChartValue("Expense", reportData?.transactions?.filter { it.transactionType == "expense" }?.sumOf { it.amount } ?: 0.0)
        )
    }

    val budgetChartData = remember(reportData) {
        reportData?.budgets_remaining?.map {
            BudgetReportData(
                name = it.budget_name,
                starting = it.starting_budget,
                remaining = it.remaining_budget
            )
        } ?: emptyList()
    }

    LaunchedEffect(familyView, startDate, endDate) {
        fetchData()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        DateRangeFilterForm(
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = { startDate = it },
            onEndDateChange = { endDate = it },
            onSubmit = {
                fetchData()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Budget Charts", style = MaterialTheme.typography.titleMedium)

        if (pieChartData.isNotEmpty() || incomeExpenseData.any { it.value > 0.0 } || budgetChartData.isNotEmpty()) {
            BudgetCharts(
                pieChartData = pieChartData,
                incomeExpenseData = incomeExpenseData,
                budgetData = budgetChartData
            )
        } else {
            ChartDataError()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Actions", style = MaterialTheme.typography.titleMedium)

        BudgetActions(
            onAddBudget = { handleOpen("addBudget") },
            onEditBudget = { handleOpen("editBudget") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Your Budgets", style = MaterialTheme.typography.titleMedium)

        if (existingBudgets.isNotEmpty()) {
            BudgetList(
                budgets = existingBudgets,
                onViewHistory = { id -> handleOpen("viewHistory", id) },
                onSetGoal = { id -> handleOpen("setBudgetGoal", id) }
            )
        } else {
            ChartDataError()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Financial Overview", style = MaterialTheme.typography.titleMedium)

        if (incomeExpenseData.any { it.value > 0.0 } || budgetChartData.isNotEmpty()) {
            FinancialOverview(
                startDate = startDate.value,
                endDate = endDate.value,
                income = incomeExpenseData[0].value,
                expense = incomeExpenseData[1].value,
                remaining = budgetChartData.sumOf { it.remaining }
            )
        } else {
            ChartDataError()
        }
    }

    if (openDialog) {
        when (modalType) {
            "addBudget" -> {
                Dialog(onDismissRequest = { handleClose() }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 4.dp
                    ) {
                        BudgetForm(
                            apiClient = apiClient,
                            onSuccess = { handleFormSuccess() }
                        )
                    }
                }
            }

            "editBudget" -> {
                Dialog(onDismissRequest = { handleClose() }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 4.dp
                    ) {
                        BudgetEditForm(
                            apiClient = apiClient,
                            onSuccess = { handleFormSuccess() }
                        )
                    }
                }
            }

            "viewHistory" -> FormDialog(
                title = "Account History",
                fullWidth = true,
                onDismiss = { handleClose() }
            ) {
                selectedBudgetId?.let {
                    BudgetHistory(budgetId = it, apiClient = apiClient, familyView = familyView)
                }
            }


            "setBudgetGoal" -> {
                Dialog(onDismissRequest = { handleClose() }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 4.dp
                    ) {
                        BudgetGoalForm(
                            apiClient = apiClient,
                            onSuccess = { handleFormSuccess() }
                        )
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        SuccessDialog(
            onDismiss = { showSuccessDialog = false }
        )
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    }

    errorMessage?.let {
        AlertHandler(alertMessage = it)
    }
}
