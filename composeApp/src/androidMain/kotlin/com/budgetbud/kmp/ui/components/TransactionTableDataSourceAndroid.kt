package com.budgetbud.kmp.ui.components

import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.TransactionHistoryTableData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.http.*

actual class TransactionTableDataSource(private val apiClient: ApiClient) {

    actual suspend fun fetchHistory(
        startDate: String,
        endDate: String,
        familyView: Boolean,
        asPdf: Boolean
    ): List<TransactionHistoryTableData> {
        val tokens = apiClient.getTokens()
        val queryParams = listOf("familyView" to familyView.toString())
        val payload = buildMap {
            put("start_date", startDate)
            put("end_date", endDate)
            if (asPdf) put("format", "pdf")
        }

        return apiClient.client.post("https://api.budgetingbud.com/api/transaction-table-view/") {
            contentType(ContentType.Application.Json)
            setBody(payload)
            headers {
                tokens?.let {
                    append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                }
            }
            url { queryParams.forEach { (k, v) -> parameters.append(k, v) } }
        }.body()
    }

    actual suspend fun update(transaction: TransactionHistoryTableData) {
        val tokens = apiClient.getTokens()
        apiClient.client.put("https://api.budgetingbud.com/api/transaction/${transaction.id}/") {
            contentType(ContentType.Application.Json)
            setBody(transaction)
            headers {
                tokens?.let {
                    append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
                }
            }
        }
    }

    actual suspend fun delete(transactionId: Int) {
        val tokens = apiClient.getTokens()
        apiClient.client.delete("https://api.budgetingbud.com/api/transaction/$transactionId/")
        headers {
            tokens?.let {
                append(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
            }
        }
    }
}
