package com.budgetbud.kmp.utils

import kotlinx.datetime.LocalDate as KxLocalDate
import java.time.LocalDate as JavaLocalDate

fun KxLocalDate.toJavaLocalDate(): JavaLocalDate =
    JavaLocalDate.of(this.year, this.monthNumber, this.dayOfMonth)

fun JavaLocalDate.toKotlinLocalDate(): KxLocalDate =
    KxLocalDate(this.year, this.monthValue, this.dayOfMonth)