package com.budgetbud.kmp.auth

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual fun provideHttpClientEngine(): HttpClientEngine = CIO.create()