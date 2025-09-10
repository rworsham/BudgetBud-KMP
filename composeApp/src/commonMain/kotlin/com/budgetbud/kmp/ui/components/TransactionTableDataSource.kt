package com.budgetbud.kmp.ui.components

import com.budgetbud.kmp.models.TransactionHistoryTableData

expect class TransactionTableDataSource {
    suspend fun fetchHistory(
        startDate: String,
        endDate: String,
        familyView: Boolean,
        asPdf: Boolean = false
    ): List<TransactionHistoryTableData>

    suspend fun update(transaction: TransactionHistoryTableData)
    suspend fun delete(transactionId: Int)
}
