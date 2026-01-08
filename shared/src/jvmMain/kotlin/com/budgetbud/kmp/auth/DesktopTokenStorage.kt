package com.budgetbud.kmp.auth

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import kotlin.random.Random

actual val tokenStorage: TokenStorage by lazy {
    DesktopTokenStorage(username = System.getProperty("user.name"), appId = "BudgetBud")
}

class DesktopTokenStorage(
    private val username: String,
    private val appId: String
) : TokenStorage {

    private val tokenFile: Path = resolveTokenFile(appId)
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
    private val encryptionKey: SecretKey = deriveKey(username, appId)

    companion object {
        private const val AES_KEY_SIZE = 256
        private const val GCM_IV_SIZE = 12
        private const val GCM_TAG_SIZE = 128

        private fun resolveTokenFile(appId: String): Path {
            val home = System.getProperty("user.home")
            val configDir = when {
                System.getProperty("os.name").startsWith("Windows") ->
                    Paths.get(System.getenv("APPDATA"), appId)
                System.getProperty("os.name").startsWith("Mac") ->
                    Paths.get(home, "Library", "Application Support", appId)
                else ->
                    Paths.get(home, ".config", appId)
            }
            Files.createDirectories(configDir)
            return configDir.resolve("tokens.dat")
        }

        private fun deriveKey(username: String, appId: String): SecretKey {
            val salt = (username + appId).toByteArray(Charsets.UTF_8)
            val spec = PBEKeySpec(username.toCharArray(), salt, 100_000, AES_KEY_SIZE)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = factory.generateSecret(spec).encoded
            return SecretKeySpec(keyBytes, "AES")
        }
    }

    @Serializable
    private data class StoredTokens(val accessToken: String, val refreshToken: String?)

    override suspend fun getTokens(): TokenPair? {
        if (!Files.exists(tokenFile)) return null
        val encrypted = Files.readAllBytes(tokenFile)
        val decrypted = decrypt(encrypted) ?: return null
        val stored = json.decodeFromString<StoredTokens>(decrypted)
        return TokenPair(stored.accessToken, stored.refreshToken)
    }

    override suspend fun saveTokens(tokens: TokenPair) {
        val stored = StoredTokens(tokens.accessToken, tokens.refreshToken)
        val serialized = json.encodeToString(stored)
        val encrypted = encrypt(serialized)
        Files.write(tokenFile, encrypted)
    }

    override suspend fun clearTokens() {
        Files.deleteIfExists(tokenFile)
    }

    private fun encrypt(plainText: String): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(GCM_IV_SIZE).also { Random.nextBytes(it) }
        val spec = GCMParameterSpec(GCM_TAG_SIZE, iv)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, spec)
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return iv + cipherText
    }

    private fun decrypt(data: ByteArray): String? {
        if (data.size < GCM_IV_SIZE) return null
        val iv = data.copyOfRange(0, GCM_IV_SIZE)
        val cipherText = data.copyOfRange(GCM_IV_SIZE, data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_SIZE, iv)
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, spec)
        return cipher.doFinal(cipherText).toString(Charsets.UTF_8)
    }
}
