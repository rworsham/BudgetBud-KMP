package com.budgetbud.kmp.ui.components

import com.budgetbud.kmp.auth.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.models.AccountHistoryData
import com.budgetbud.kmp.models.AccountHistoryTableData

suspend fun fetchAccountHistory(
    apiClient: ApiClient,
    accountId: Int,
    startDate: String,
    endDate: String,
    familyView: Boolean
): List<AccountHistoryTableData> {
    val queryParams = listOf("familyView" to familyView.toString())
    val tokens = apiClient.getTokens()
    val payload = AccountHistoryData(
        account_id = accountId,
        start_date = startDate,
        end_date = endDate
    )
    return apiClient.client.post("https://api.budgetingbud.com/api/account/history/") {
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
expect fun AccountHistory(
    accountId: Int,
    apiClient: ApiClient,
    familyView: Boolean,
    modifier: Modifier = Modifier
)
