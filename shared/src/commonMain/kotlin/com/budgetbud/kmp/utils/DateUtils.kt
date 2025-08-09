package com.budgetbud.kmp.utils

import kotlinx.datetime.*

object DateUtils {
    fun firstDayOfCurrentMonth(): LocalDate {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return LocalDate(now.year, now.monthNumber, 1)
    }

    fun lastDayOfCurrentMonth(): LocalDate {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val nextMonth = if (now.monthNumber == 12) LocalDate(now.year + 1, 1, 1) else LocalDate(now.year, now.monthNumber + 1, 1)
        return nextMonth.minus(1, DateTimeUnit.DAY)
    }
}