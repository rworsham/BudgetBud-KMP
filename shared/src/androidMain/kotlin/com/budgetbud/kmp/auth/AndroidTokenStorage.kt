package com.budgetbud.kmp.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

lateinit var appContext: Context

actual val tokenStorage: TokenStorage by lazy {
    AndroidTokenStorage(appContext)
}

class AndroidTokenStorage(context: Context) : TokenStorage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "encrypted_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun getTokens(): TokenPair? {
        val accessToken = prefs.getString("accessToken", null)
        val refreshToken = prefs.getString("refreshToken", null)
        return if (accessToken != null && refreshToken != null) {
            TokenPair(accessToken, refreshToken)
        } else {
            null
        }
    }

    override suspend fun saveTokens(tokens: TokenPair) {
        prefs.edit()
            .putString("accessToken", tokens.accessToken)
            .putString("refreshToken", tokens.refreshToken)
            .apply()
    }

    override suspend fun clearTokens() {
        prefs.edit()
            .remove("accessToken")
            .remove("refreshToken")
            .apply()
    }
}
