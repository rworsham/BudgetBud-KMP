package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.datetime.LocalDate


@Serializable
data class Transaction(
    val id: Long?,
    val date: LocalDate,
    val amount: String,
    val transactionType: TransactionType,
    val description: String? = null,
    val categoryId: Long,
    val budgetId: Long,
    val accountId: Long,
    val isRecurring: Boolean,
    val recurringType: RecurringType? = null,
    val nextOccurrence: LocalDate? = null,
    val userId: Long,
    val familyId: Long? = null
)

@Serializable
enum class TransactionType {
    @SerialName("income")
    INCOME,

    @SerialName("expense")
    EXPENSE
}

@Serializable
enum class RecurringType {
    @SerialName("daily")
    DAILY,

    @SerialName("weekly")
    WEEKLY,

    @SerialName("monthly")
    MONTHLY,

    @SerialName("yearly")
    YEARLY,

    @SerialName("one-time")
    ONE_TIME
}
