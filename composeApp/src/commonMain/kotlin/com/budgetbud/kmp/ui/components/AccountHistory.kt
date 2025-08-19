package com.budgetbud.kmp.ui.components

import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.AccountHistoryData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

suspend fun fetchAccountHistory(
    apiClient: ApiClient,
    accountId: Int,
    startDate: String,
    endDate: String,
    familyView: Boolean
): List<AccountHistoryData> {
    val queryParams = listOf("familyView" to familyView.toString())
    val payload = mapOf(
        "account_id" to accountId,
        "start_date" to startDate,
        "end_date" to endDate
    )
    return apiClient.client.post("https://api.budgetingbud.com/api/account/history/") {
        contentType(ContentType.Application.Json)
        setBody(payload)
        url { queryParams.forEach { (k, v) -> parameters.append(k, v) } }
    }.body()
}

@Composable
expect fun AccountHistory(
    accountId: Int,
    apiClient: ApiClient,
    familyView: Boolean,
    modifier: Modifier = Modifier
)
