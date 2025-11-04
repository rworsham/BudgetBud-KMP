package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.CategoryOverviewData
import com.budgetbud.kmp.models.CategoryHistoryLineChartData
import com.budgetbud.kmp.ui.components.charts.CategoryLineChart
import com.budgetbud.kmp.ui.components.forms.CategoryForm
import com.budgetbud.kmp.ui.components.forms.DateRangeFilterForm
import com.budgetbud.kmp.utils.DateUtils
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CategoryOverview(
    familyView: Boolean,
    apiClient: ApiClient,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var categoryData by remember { mutableStateOf<List<CategoryOverviewData>>(emptyList()) }
    var categoryHistory by remember { mutableStateOf<List<CategoryHistoryLineChartData>>(emptyList()) }

    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    var openDialog by remember { mutableStateOf(false) }
    var modalType by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf(DateUtils.firstDayOfCurrentMonth()) }
    var endDate by remember { mutableStateOf(DateUtils.lastDayOfCurrentMonth()) }

    fun fetchData() {
        coroutineScope.launch {
            isLoading = true
            try {
                val tokens = apiClient.getTokens()
                val queryParams = listOf("familyView" to familyView.toString())
                val payload = mapOf(
                    "start_date" to startDate.toString(),
                    "end_date" to endDate.toString()
                )

                val categoryRes = apiClient.client.post("https://api.budgetingbud.com/api/category/data/") {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                    url {
                        queryParams.forEach { (k, v) -> parameters.append(k, v) }
                    }
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }.body<List<CategoryOverviewData>>()

                val historyRes = apiClient.client.post("https://api.budgetingbud.com/api/category/history/line-chart/") {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                    url {
                        queryParams.forEach { (k, v) -> parameters.append(k, v) }
                    }
                    headers {
                        tokens?.let {
                            append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                        }
                    }
                }.body<List<CategoryHistoryLineChartData>>()

                categoryData = categoryRes
                categoryHistory = historyRes
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch category data"
            } finally {
                isLoading = false
            }
        }
    }

    fun handleOpen(type: String, categoryId: Int? = null) {
        modalType = type
        selectedCategoryId = categoryId
        openDialog = true
    }

    fun handleClose() {
        modalType = ""
        openDialog = false
        selectedCategoryId = null
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
            onStartDateChange = { newStartDate ->
                startDate = newStartDate
                fetchData()
            },
            onEndDateChange = { newEndDate ->
                endDate = newEndDate
                fetchData()
            },
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            thickness = 2.dp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { handleOpen("addCategory") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Category")
        }

        Spacer(modifier = Modifier.height(4.dp))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            thickness = 2.dp
        )

        if (categoryData.isNotEmpty()) {
            CategoryCardList(
                categories = categoryData,
                onViewHistory = { id -> handleOpen("viewHistory", id) }
            )
        } else {
            Text("No category data found.")
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            thickness = 2.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (categoryHistory.isNotEmpty()) {
            CategoryLineChart(
                historyData = categoryHistory,
                categoryData = categoryData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

        }

        Button(onClick = {}) {
            Text("Download as PDF")
            // TBD
        }
    }

    if (openDialog) {
        when (modalType) {
            "addCategory" -> {
                Dialog(onDismissRequest = { handleClose() }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        CategoryForm(
                            apiClient = apiClient,
                            onSuccess = { handleFormSuccess() }
                        )
                    }
                }
            }

            "viewHistory" -> {
                selectedCategoryId?.let {
                    FormDialog(title = "Category History", onDismiss = { handleClose() }) {
                        CategoryHistory(categoryId = it, apiClient = apiClient, familyView = familyView)
                    }
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
                .size(144.dp))
        }
    }

    errorMessage?.let {
        AlertHandler(alertMessage = it)
    }
}
