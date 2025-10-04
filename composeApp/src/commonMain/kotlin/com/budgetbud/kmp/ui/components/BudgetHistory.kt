package com.budgetbud.kmp.ui.components

import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.TransactionHistoryTableData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

suspend fun fetchBudgetHistory(
    apiClient: ApiClient,
    budgetId: Int,
    startDate: String,
    endDate: String,
    familyView: Boolean,
    asPdf: Boolean = false
): List<TransactionHistoryTableData> {
    val queryParams = listOf("familyView" to familyView.toString())
    val tokens = apiClient.getTokens()
    val payload = buildMap {
        put("budget_id", budgetId)
        put("start_date", startDate)
        put("end_date", endDate)
        if (asPdf) put("format", "pdf")
    }

    return apiClient.client.post("https://api.budgetingbud.com/api/budget-history/") {
        contentType(ContentType.Application.Json)
        setBody(payload)
        url { queryParams.forEach { (k, v) -> parameters.append(k, v) } }
        headers {
            tokens?.let {
                append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
            }
        }
    }.body()
}

@Composable
expect fun BudgetHistory(
    budgetId: Int,
    apiClient: ApiClient,
    familyView: Boolean,
    modifier: Modifier = Modifier
)
