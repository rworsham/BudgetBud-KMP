package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.AccountData
import com.budgetbud.kmp.models.AccountOverviewData
import com.budgetbud.kmp.ui.components.forms.AccountForm
import com.budgetbud.kmp.ui.components.forms.DateRangeFilterForm
import com.budgetbud.kmp.ui.components.forms.SavingsGoalForm
import com.budgetbud.kmp.utils.DateUtils
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AccountOverview(
    familyView: Boolean,
    apiClient: ApiClient,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var accountData by remember { mutableStateOf<List<AccountData>>(emptyList()) }
    var accountHistory by remember { mutableStateOf<List<AccountOverviewData>>(emptyList()) }

    var openDialog by remember { mutableStateOf(false) }
    var modalType by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf<Int?>(null) }

    var startDate by remember { mutableStateOf(DateUtils.firstDayOfCurrentMonth()) }
    var endDate by remember { mutableStateOf(DateUtils.lastDayOfCurrentMonth()) }

    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun fetchData() {
        coroutineScope.launch {
            isLoading = true
            try {
                val queryParams = listOf("familyView" to familyView.toString())
                val payload = mapOf(
                    "start_date" to startDate.toString(),
                    "end_date" to endDate.toString()
                )

                val resAccounts = apiClient.client.get("https://api.budgetingbud.com/api/accounts/") {
                    url { queryParams.forEach { (k, v) -> parameters.append(k, v) } }
                }.body<List<AccountData>>()

                val resHistory = apiClient.client.post("https://api.budgetingbud.com/api/accounts/overview-report/") {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                    url { queryParams.forEach { (k, v) -> parameters.append(k, v) } }
                }.body<List<AccountOverviewData>>()

                accountData = resAccounts
                accountHistory = resHistory
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch account data"
            } finally {
                isLoading = false
            }
        }
    }

    fun handleOpen(type: String, accountId: Int? = null) {
        modalType = type
        openDialog = true
        selectedAccountId = accountId
    }

    fun handleClose() {
        modalType = ""
        openDialog = false
        selectedAccountId = null
        showSuccessDialog = false
    }

    fun handleFormSuccess() {
        showSuccessDialog = true
        coroutineScope.launch {
            delay(3000)
            handleClose()
            fetchData()
        }
    }

    LaunchedEffect(startDate, endDate, familyView) {
        fetchData()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        DateRangeFilterForm(
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = { startDate = it },
            onEndDateChange = { endDate = it },
            onSubmit = { fetchData() }
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { handleOpen("addAccount") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Account")
        }

        Spacer(Modifier.height(16.dp))

        if (accountData.isNotEmpty()) {
            AccountCardList(
                accounts = accountData,
                onViewHistory = { id -> handleOpen("viewHistory", id) },
                onSetGoal = { id -> handleOpen("setSavingsGoal", id) }
            )
        } else {
            Text("No accounts available.")
        }

        Spacer(Modifier.height(16.dp))

        if (accountHistory.isNotEmpty()) {
            AccountLineChart(accountHistory, accountData)
        }

        Button(onClick = {
            // TBD
        }) {
            Text("Download as PDF")
        }
    }

    // Dialogs
    if (openDialog) {
        when (modalType) {
            "addAccount" -> {
                Dialog(onDismissRequest = { handleClose() }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 4.dp
                    ) {
                        AccountForm(apiClient = apiClient, onSuccess = { handleFormSuccess() })
                    }
                }
            }

            "viewHistory" -> {
                selectedAccountId?.let {
                    FormDialog(title = "Account History", onDismiss = { handleClose() }) {
                        AccountHistory(accountId = it, apiClient = apiClient, familyView = familyView)
                    }
                }
            }

            "setSavingsGoal" -> {
                selectedAccountId?.let {
                    FormDialog(title = "Set Savings Goal", onDismiss = { handleClose() }) {
                        SavingsGoalForm(accountId = it, apiClient = apiClient, onSuccess = { handleFormSuccess() })
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        SuccessDialog(onDismiss = { showSuccessDialog = false })
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    }

    errorMessage?.let {
        AlertHandler(alertMessage = it)
    }
}
