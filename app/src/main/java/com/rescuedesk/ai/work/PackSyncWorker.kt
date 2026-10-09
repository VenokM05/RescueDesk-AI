package com.rescuedesk.ai.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.data.pack.PackStatus
import java.util.concurrent.TimeUnit

/**
 * Daily background guide-pack check (PRD section 5.12). Respects the user's
 * Wi-Fi-only preference; a manual "Check for updates" in Screen L bypasses
 * the periodic schedule but runs the same serialized sync.
 */
class PackSyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val status = ServiceLocator.packRepository.sync()
        return when (status) {
            is PackStatus.UpToDate -> Result.success(
                workDataOf(KEY_DOWNLOADED to status.downloaded)
            )
            // Transient network problems: retry with backoff. Content problems
            // (checksum, rights, schema) will not fix themselves: stop retrying.
            is PackStatus.Failed -> {
                val transient = status.reason.contains("Network", ignoreCase = true) ||
                    status.reason.contains("HTTP", ignoreCase = true)
                if (transient && runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
            }
            else -> Result.failure()
        }
    }

    companion object {
        const val UNIQUE_NAME = "pack-sync-daily"
        const val KEY_DOWNLOADED = "downloaded"
        private const val MAX_ATTEMPTS = 3

        fun constraints(wifiOnly: Boolean): Constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        fun periodicRequest(wifiOnly: Boolean) =
            PeriodicWorkRequestBuilder<PackSyncWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints(wifiOnly))
                .setInitialDelay(6, TimeUnit.HOURS)
                .build()
    }
}
