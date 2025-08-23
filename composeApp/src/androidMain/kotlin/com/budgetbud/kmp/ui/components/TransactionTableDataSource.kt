package com.budgetbud.kmp.ui.components

import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.TransactionHistoryTableData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

actual class TransactionTableDataSource(private val apiClient: ApiClient) {

    actual suspend fun fetchHistory(
        budgetId: Int,
        startDate: String,
        endDate: String,
        familyView: Boolean,
        asPdf: Boolean
    ): List<TransactionHistoryTableData> {
        val queryParams = listOf("familyView" to familyView.toString())
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
        }.body()
    }

    actual suspend fun update(transaction: TransactionHistoryTableData) {
        apiClient.client.put("https://api.budgetingbud.com/api/transaction/${transaction.id}/") {
            contentType(ContentType.Application.Json)
            setBody(transaction)
        }
    }

    actual suspend fun delete(transactionId: Int) {
        apiClient.client.delete("https://api.budgetingbud.com/api/transaction/$transactionId/")
    }
}
