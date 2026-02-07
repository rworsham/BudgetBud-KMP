package com.budgetbud.kmp.ui.components

import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.TransactionHistoryTableData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class TransactionTableDataSource(private val apiClient: ApiClient) {

    actual suspend fun fetchHistory(
        startDate: String,
        endDate: String,
        familyView: Boolean,
        asPdf: Boolean
    ): List<TransactionHistoryTableData> = withContext(Dispatchers.IO) {
        val tokens = apiClient.getTokens()
        val payload = buildMap {
            put("start_date", startDate)
            put("end_date", endDate)
            if (asPdf) put("format", "pdf")
        }

        try {
            apiClient.client.post("https://api.budgetingbud.com/api/transaction-table-view/") {
                contentType(ContentType.Application.Json)
                setBody(payload)
                headers {
                    tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") }
                }
                url {
                    parameters.append("familyView", familyView.toString())
                }
            }.body()
        } catch (e: Exception) {
            println("Error fetching transaction history: ${e.message}")
            emptyList()
        }
    }

    actual suspend fun update(transaction: TransactionHistoryTableData) = withContext(Dispatchers.IO) {
        val tokens = apiClient.getTokens()
        try {
            apiClient.client.put("https://api.budgetingbud.com/api/transaction/${transaction.id}/") {
                contentType(ContentType.Application.Json)
                setBody(transaction)
                headers {
                    tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") }
                }
            }
            Unit
        } catch (e: Exception) {
            println("Error updating transaction ${transaction.id}: ${e.message}")
        }
    }

    actual suspend fun delete(transactionId: Int) = withContext(Dispatchers.IO) {
        val tokens = apiClient.getTokens()
        try {
            apiClient.client.delete("https://api.budgetingbud.com/api/transaction/$transactionId/") {
                headers {
                    tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") }
                }
            }
            Unit
        } catch (e: Exception) {
            println("Error deleting transaction $transactionId: ${e.message}")
        }
    }
}