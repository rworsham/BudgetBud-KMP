package com.budgetbud.kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform