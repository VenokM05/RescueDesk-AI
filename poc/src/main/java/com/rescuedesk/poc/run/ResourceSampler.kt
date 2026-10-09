package com.rescuedesk.poc.run

import android.content.Context
import android.os.PowerManager
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Harness piece section 5.3: 1 Hz memory + thermal capture during runs, plus the
 * section 5.4 unload-discipline check (RSS must return to baseline after unload).
 */
class ResourceSampler(private val context: Context) {

    data class Sample(val rssKb: Long, val thermal: String)

    private val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var baselineRssKb: Long = 0

    fun thermalStatus(): String = when (power.currentThermalStatus) {
        PowerManager.THERMAL_STATUS_NONE -> "NONE"
        PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT"
        PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
        PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE"
        PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
        else -> "OTHER"
    }

    /** Our own process RSS from /proc/self/status (VmRSS), in kB. */
    fun rssKb(): Long = runCatching {
        BufferedReader(InputStreamReader(java.io.FileInputStream("/proc/self/status")))
            .useLines { lines ->
                lines.firstOrNull { it.startsWith("VmRSS:") }
                    ?.filter { it.isDigit() }?.toLong() ?: 0L
            }
    }.getOrDefault(0L)

    fun captureBaseline() {
        baselineRssKb = rssKb()
    }

    fun baselineRssKb(): Long = baselineRssKb

    /** section 5.4: after unload(), poll RSS up to 10 s; returns residual delta kB. */
    suspend fun waitForRelease(): Long {
        repeat(10) {
            val delta = rssKb() - baselineRssKb
            if (delta <= 20_000) return delta.coerceAtLeast(0)
            delay(1_000)
        }
        return (rssKb() - baselineRssKb).coerceAtLeast(0)
    }
}
