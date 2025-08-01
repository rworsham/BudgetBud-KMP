package com.budgetbud.kmp.auth

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.datetime.Clock
import kotlin.io.encoding.Base64

object JwtUtils {
    fun isExpired(jwt: String): Boolean {
        return try {
            val payload = jwt.split(".")[1]
            val decoded = decodeBase64(payload)
            val jsonElement = Json.parseToJsonElement(decoded).jsonObject["exp"]
            val exp = (jsonElement as? JsonPrimitive)?.content?.toLongOrNull() ?: return true
            val now = Clock.System.now().epochSeconds
            now >= exp
        } catch (e: Exception) {
            true
        }
    }

    private fun decodeBase64(base64: String): String {
        return base64
            .replace('-', '+')
            .replace('_', '/')
            .let {
                val pad = (4 - it.length % 4) % 4
                val padded = it + "=".repeat(pad)
                Base64.decode(padded).decodeToString()
            }
    }
}
