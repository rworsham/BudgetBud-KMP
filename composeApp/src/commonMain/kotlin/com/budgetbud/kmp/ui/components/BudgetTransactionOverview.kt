package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.budgetbud.kmp.models.BudgetRemainingBudgetBarChartData
import com.budgetbud.kmp.models.BudgetReportData
import com.budgetbud.kmp.models.ExpenseCategoriesPieChartData
import com.budgetbud.kmp.models.IncomeExpenseBarChartData
import com.budgetbud.kmp.ui.components.charts.IncomeExpenseBudgetBarChart
import com.budgetbud.kmp.ui.components.charts.ExpenseCategoriesBudgetPieChart
import com.budgetbud.kmp.ui.components.charts.BudgetRemainingBarChart
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
    var pieChartData by remember { mutableStateOf<List<ExpenseCategoriesPieChartData>>(emptyList()) }

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
            if (!apiClient.isLoggedIn.value) return@launch

            isLoading = true
            try {
                val queryParams = listOf("familyView" to familyView.toString())
                val tokens = apiClient.getTokens()
                val datePayload = mapOf(
                    "start_date" to startDate.toString(),
                    "end_date" to endDate.toString()
                )

                fun HttpRequestBuilder.attachAuthAndQueryParams() {
                    contentType(ContentType.Application.Json)
                    setBody(datePayload)
                    url {
                        queryParams.forEach { (key, value) ->
                            parameters.append(key, value)
                        }
                    }
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }

                val reportResponse = apiClient.client.post("https://api.budgetingbud.com/api/budget-transaction-overview/") {
                    attachAuthAndQueryParams()
                }.body<BudgetReportData>()

                val pieResponse = apiClient.client.post("https://api.budgetingbud.com/api/transaction-pie-chart/") {
                    attachAuthAndQueryParams()
                }.body<List<ExpenseCategoriesPieChartData>>()

                val budgetsResponse = apiClient.client.get("https://api.budgetingbud.com/api/budget/") {
                    attachAuthAndQueryParams()
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
            IncomeExpenseBarChartData("Income", reportData?.transactions?.filter { it.transaction_type == "income" }?.sumOf { it.amount.toDouble() } ?: 0.0),
            IncomeExpenseBarChartData("Expense", reportData?.transactions?.filter { it.transaction_type == "expense" }?.sumOf { it.amount.toDouble() } ?: 0.0)
        )
    }

    val budgetChartData = remember(reportData) {
        reportData?.budgets_remaining?.map { budget ->
            BudgetRemainingBudgetBarChartData(
                name = budget.budget_name,
                starting_budget = budget.starting_budget.toDoubleOrNull() ?: 0.0,
                remaining_budget = budget.remaining_budget.toDoubleOrNull() ?: 0.0
            )
        } ?: emptyList()
    }

    LaunchedEffect(familyView, startDate, endDate) {
        fetchData()
    }

    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
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


        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = { handleOpen("addBudget") }) {
                Text("Add Budget")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { handleOpen("editBudget") }) {
                Text("Edit Budget")
            }
        }

        if (openDialog) {
            when (modalType) {
                "addBudget" -> {
                    Dialog(onDismissRequest = { handleClose() }) {
                        Surface(modifier = Modifier.padding(16.dp)) {
                            BudgetForm(
                                apiClient = apiClient,
                                onSuccess = { handleFormSuccess() }
                            )
                        }
                    }
                }
                "editBudget" -> {
                    Dialog(onDismissRequest = { handleClose() }) {
                        Surface(modifier = Modifier.padding(16.dp)) {
                            BudgetEditForm(
                                apiClient = apiClient,
                                onSuccess = { handleFormSuccess() }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Your Budgets", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (pieChartData.isNotEmpty()) {
                ExpenseCategoriesBudgetPieChart(
                    data = pieChartData,
                    modifier = Modifier.weight(1f)
                )
            } else {
                ChartDataError()
            }


            if (incomeExpenseData.any { it.value > 0 }) {
                IncomeExpenseBudgetBarChart(
                    data = incomeExpenseData,
                    modifier = Modifier.weight(1f)
                )
            } else {
                ChartDataError()
            }


            if (budgetChartData.isNotEmpty()) {
                BudgetRemainingBarChart(
                    data = budgetChartData,
                    modifier = Modifier.weight(1f)
                )
            } else {
                ChartDataError()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Financial Overview", style = MaterialTheme.typography.titleMedium)

        if (incomeExpenseData.any { it.value > 0.0 } || budgetChartData.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = "Financial Overview",
                        style = MaterialTheme.typography.titleMedium.copy(
                        )
                    )

                    Text(
                        text = "$startDate - $endDate",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )

                    Text(
                        text = "Total Income: $${"%.2f".format(incomeExpenseData[0].value)}",
                    )

                    Text(
                        text = "Total Expenses: $${"%.2f".format(incomeExpenseData[1].value)}",
                    )

                    Text(
                        text = "Remaining Budget: $${"%.2f".format(budgetChartData.sumOf { it.remaining_budget })}",
                    )
                }
            }
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
                        selectedBudgetId?.let {
                            BudgetGoalForm(budgetId = it, apiClient = apiClient, onSuccess = {showSuccessDialog = true})
                        }
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
