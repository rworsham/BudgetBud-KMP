package com.budgetbud.kmp

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AndroidTokenStorage(context: Context) : TokenStorage {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_token_prefs",
        MasterKey.Builder(context)
            .setKIeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferencs.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferencs.PrefValueEncryptionScheme.AES256_GCM,
    )


}