package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.budgetbud.kmp.auth.models.User
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.FamilyTransactionOverviewData
import com.budgetbud.kmp.models.FamilyCategoryOverviewData
import com.budgetbud.kmp.ui.components.forms.DateRangeFilterForm
import com.budgetbud.kmp.ui.components.forms.FamilyCreateForm
import com.budgetbud.kmp.ui.components.forms.FamilyInviteForm
import com.budgetbud.kmp.utils.DateUtils
import com.budgetbud.kmp.ui.components.charts.FamilyCategoryBarChart
import com.budgetbud.kmp.ui.components.charts.FamilyTransactionBarChart
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.call.body
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FamilyOverview(
    apiClient: ApiClient,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var familyData by remember { mutableStateOf<List<User>>(emptyList()) }
    var transactionOverview by remember { mutableStateOf<List<FamilyTransactionOverviewData>>(emptyList()) }
    var categoryOverview by remember { mutableStateOf<List<FamilyCategoryOverviewData>>(emptyList()) }

    var openDialog by remember { mutableStateOf(false) }
    var modalType by remember { mutableStateOf("") }
    var selectedUserId by remember { mutableStateOf<String?>(null) }

    var startDate by remember { mutableStateOf(DateUtils.firstDayOfCurrentMonth()) }
    var endDate by remember { mutableStateOf(DateUtils.lastDayOfCurrentMonth()) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun fetchData() {
        coroutineScope.launch {
            isLoading = true
            try {
                val tokens = apiClient.getTokens()
                val datePayload = mapOf(
                    "start_date" to startDate.toString(),
                    "end_date" to endDate.toString()
                )

                familyData = apiClient.client.get("https://api.budgetingbud.com/api/family/").body()

                if (familyData.isNotEmpty()) {
                    transactionOverview = apiClient.client.post("https://api.budgetingbud.com/api/family/overview/") {
                        contentType(ContentType.Application.Json)
                        setBody(datePayload)
                        url { parameters.append("Transaction", "true") }
                        headers {
                            tokens?.let {
                                append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                            }
                        }
                    }.body()

                    categoryOverview = apiClient.client.post("https://api.budgetingbud.com/api/family/overview/") {
                        contentType(ContentType.Application.Json)
                        setBody(datePayload)
                        url { parameters.append("Category", "true") }
                        headers {
                            tokens?.let {
                                append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                            }
                        }
                    }.body()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun handleOpen(type: String, userId: String? = null) {
        modalType = type
        selectedUserId = userId
        openDialog = true
    }

    fun handleClose() {
        modalType = ""
        selectedUserId = null
        openDialog = false
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


    LaunchedEffect(startDate, endDate, showSuccessDialog) {
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

        if (familyData.isNotEmpty()) {
            Button(
                onClick = { handleOpen("addAccount") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Invite New Member")
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn {
                items(familyData) { user ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(user.username, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { handleOpen("viewHistory", user.id) }) {
                                Text("View User History")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Contributions Per User", style = MaterialTheme.typography.titleMedium)
            if (transactionOverview.isNotEmpty()) {
                FamilyTransactionBarChart(data = transactionOverview)
            } else {
                ChartDataError()
            }

            Spacer(Modifier.height(24.dp))

            Text("Category Usage Per User", style = MaterialTheme.typography.titleMedium)
            if (categoryOverview.isNotEmpty()) {
                FamilyCategoryBarChart(data = categoryOverview)
            } else {
                ChartDataError()
            }

        } else {
            Button(
                onClick = { handleOpen("createFamily") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Family Group")
            }
        }
    }

    if (openDialog) {
        when (modalType) {
            "addAccount" -> {
                FormDialog(title = "Invite New Member", onDismiss = { handleClose() }) {
                    FamilyInviteForm(apiClient = apiClient, onSuccess = { handleFormSuccess() })
                }
            }

            "createFamily" -> {
                FormDialog(title = "Create Family Group", onDismiss = { handleClose() }) {
                    FamilyCreateForm(apiClient = apiClient, onSuccess = { handleFormSuccess() })
                }
            }

            "viewHistory" -> {
                selectedUserId?.let { id ->
                    FormDialog(title = "Family History", onDismiss = { handleClose() }) {
                        FamilyHistory(userId = id, apiClient = apiClient)
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
