package com.rescuedesk.ai.data.pack

import com.rescuedesk.ai.data.local.GuideDao
import com.rescuedesk.ai.data.local.GuideEntity
import com.rescuedesk.ai.data.local.PreferencesStore
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

/**
 * Guide-pack sync pipeline (docs/ARCHITECTURE.md §7, PRD §5.12):
 *
 *   manifest → per-guide download → sha256 verify → rights gate →
 *   build full new set (fresh + unchanged existing rows) → atomic activation
 *   in one transaction → record last-sync date.
 *
 * Failure at any step before activation leaves the previously installed pack
 * untouched. Built-in guides (PRD §5.4) are never modified here.
 */
class PackRepository(
    private val guideDao: GuideDao,
    private val preferences: PreferencesStore,
    private val baseUrl: String = PackConfig.BASE_URL
) {

    private val mutex = Mutex()
    private val _status = MutableStateFlow<PackStatus>(PackStatus.Idle)
    val status: StateFlow<PackStatus> = _status.asStateFlow()

    /** Pack guide count as a live flow for Screen L. */
    val packGuideCount: Flow<Int> =
        guideDao.observeAll().map { list -> list.count { !it.isBuiltin } }

    suspend fun lastSyncDate(): String? = preferences.lastPackSync()

    /** Runs a full check-and-sync. Safe to call from UI and worker; serialized. */
    suspend fun sync(): PackStatus = mutex.withLock {
        _status.value = PackStatus.Checking
        try {
            val manifestText = PackDownloader.fetchText("$baseUrl/manifest.json", PackConfig.MAX_MANIFEST_BYTES)
            val manifest = PackManifest.parse(JSONObject(manifestText))

            val newSet = mutableListOf<GuideEntity>()
            var downloaded = 0
            var skipped = 0
            manifest.guides.forEachIndexed { index, ref ->
                _status.value = PackStatus.Downloading(index, manifest.guides.size)
                val existing = guideDao.byPublicId(ref.publicId, ref.language)
                val reuse = existing?.takeIf {
                    !it.isBuiltin && it.packVersion >= ref.version && it.version >= ref.version
                }
                if (reuse != null) {
                    newSet += reuse.copy(id = 0)
                    skipped++
                    return@forEachIndexed
                }
                val bytes = PackDownloader.fetchBytes("$baseUrl/${ref.url}", PackConfig.MAX_GUIDE_BYTES)
                val sum = PackDownloader.sha256Hex(bytes)
                if (sum != ref.sha256) {
                    // Checksum mismatch is a hard stop: never activate unverified content.
                    throw PackDownloader.PackDownloadException(
                        "Checksum mismatch for ${ref.publicId} (${ref.language})"
                    )
                }
                val entity = parseGuideJson(String(bytes, Charsets.UTF_8), ref)
                // Rights gate (PRD §10.3): only reviewer-cleared content may be installed.
                if (entity.rightsStatus != "cleared") {
                    throw PackDownloader.PackDownloadException(
                        "Guide ${ref.publicId} is not rights-cleared; pack rejected"
                    )
                }
                newSet += entity
                downloaded++
            }

            _status.value = PackStatus.Applying
            guideDao.replacePackGuides(newSet)
            preferences.setLastPackSync(LocalDate.now().toString())
            PackStatus.UpToDate(downloaded, skipped).also { _status.value = it }
        } catch (e: Exception) {
            val reason = when (e) {
                is PackDownloader.PackDownloadException -> e.message ?: "Download failed"
                is IllegalArgumentException -> e.message ?: "Invalid pack manifest"
                is java.io.IOException -> "Network unavailable"
                else -> "Sync failed unexpectedly"
            }
            PackStatus.Failed(reason).also { _status.value = it }
        }
    }

    /** Guide JSON shares the built-in asset schema; parsed the same way. */
    private fun parseGuideJson(text: String, ref: PackGuideRef): GuideEntity {
        val json = JSONObject(text)
        return GuideEntity(
            id = 0,
            publicId = json.getString("publicId"),
            title = json.getString("title"),
            category = json.getString("category"),
            summary = json.getString("summary"),
            body = json.getJSONArray("steps").let { steps ->
                (0 until steps.length()).joinToString("\n") { "${it + 1}. ${steps.getString(it)}" }
            },
            avoid = json.optJSONArray("avoid")?.let { avoid ->
                (0 until avoid.length()).joinToString("\n") { avoid.getString(it) }
            }.orEmpty(),
            language = json.optString("language", ref.language),
            sourceName = json.getString("sourceName"),
            sourceRef = json.optString("sourceRef").takeIf { it.isNotEmpty() },
            publishedDate = json.optString("publishedDate").takeIf { it.isNotEmpty() },
            lastReviewed = json.optString("lastReviewed").takeIf { it.isNotEmpty() },
            nextReview = json.optString("nextReview").takeIf { it.isNotEmpty() },
            version = json.optInt("version", ref.version),
            rightsStatus = json.optString("rightsStatus", "pending"),
            isBuiltin = false,
            packVersion = ref.version
        )
    }
}

/** Pack endpoint. Replace with the production content-repo raw URL when live. */
object PackConfig {
    // Placeholder: public content repo served as static files (no backend).
    const val BASE_URL =
        "https://raw.githubusercontent.com/VenokM05/rescuedesk-content/main/pack"
    const val MAX_MANIFEST_BYTES = 256L * 1024
    const val MAX_GUIDE_BYTES = 2L * 1024 * 1024
}
