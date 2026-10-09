package com.rescuedesk.ai.data.pack

import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Minimal HTTPS fetcher for pack content. Deliberately no third-party HTTP
 * dependency while the surface is text-only; switch to a resumable client
 * when Phase 3 adds multi-GB model files (docs/ARCHITECTURE.md section 7).
 *
 * Safety posture: HTTPS-only, hard byte caps, sha256 verification by caller.
 */
object PackDownloader {

    class PackDownloadException(message: String) : Exception(message)

    suspend fun fetchBytes(url: String, maxBytes: Long): ByteArray = withContext(Dispatchers.IO) {
        require(url.startsWith("https://")) { "Pack downloads are HTTPS-only" }
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            val code = conn.responseCode
            if (code !in 200..299) throw PackDownloadException("HTTP $code for ${shortName(url)}")
            conn.inputStream.use { input ->
                val buffer = java.io.ByteArrayOutputStream()
                val chunk = ByteArray(64 * 1024)
                var total = 0L
                while (true) {
                    val n = input.read(chunk)
                    if (n <= 0) break
                    total += n
                    if (total > maxBytes) throw PackDownloadException("File too large: ${shortName(url)}")
                    buffer.write(chunk, 0, n)
                }
                buffer.toByteArray()
            }
        } finally {
            conn.disconnect()
        }
    }

    /** Fetch a small text artifact (manifest or guide JSON) with a byte cap. */
    suspend fun fetchText(url: String, maxBytes: Long): String =
        String(fetchBytes(url, maxBytes), Charsets.UTF_8)

    fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }

    private fun shortName(url: String) = url.substringAfterLast('/')
}
