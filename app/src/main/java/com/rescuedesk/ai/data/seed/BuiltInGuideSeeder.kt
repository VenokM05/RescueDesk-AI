package com.rescuedesk.ai.data.seed

import android.content.Context
import com.rescuedesk.ai.data.local.GuideDao
import com.rescuedesk.ai.data.local.GuideEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Seeds the built-in minimum guide set (PRD section 5.4) from APK assets into Room on
 * first launch, so guide access and search work with zero network and zero downloads.
 *
 * NOTE: the asset guides under assets/guides/built-in/ now carry real, original
 * emergency-preparedness content. Every item still ships with rightsStatus
 * "pending" until a qualified Subject-Matter Reviewer signs off and offline
 * redistribution rights are confirmed (PRD section 8.3, section 10.3). The
 * in-app "pending review" banner surfaces that honestly — do NOT mark an item
 * "cleared" without a recorded reviewer and approval status.
 */
class BuiltInGuideSeeder(
    private val context: Context,
    private val guideDao: GuideDao
) {

    suspend fun seedIfNeeded() {
        if (guideDao.count() > 0) return
        withContext(Dispatchers.IO) {
            val dir = "guides/built-in"
            val files = context.assets.list(dir)?.toList().orEmpty().sorted()
            for (name in files.filter { it.endsWith(".json") }) {
                val json = context.assets.open("$dir/$name")
                    .reader(Charsets.UTF_8).readText().let(::JSONObject)
                guideDao.insertIfMissing(
                    GuideEntity(
                        publicId = json.getString("publicId"),
                        title = json.getString("title"),
                        category = json.getString("category"),
                        summary = json.getString("summary"),
                        body = json.getJSONArray("steps").let { steps ->
                            (0 until steps.length())
                                .joinToString("\n") { "${it + 1}. ${steps.getString(it)}" }
                        },
                        avoid = json.optJSONArray("avoid")?.let { avoid ->
                            (0 until avoid.length()).joinToString("\n") { avoid.getString(it) }
                        }.orEmpty(),
                        language = json.optString("language", "en"),
                        sourceName = json.optString("sourceName", "RescueDesk AI (unreviewed)"),
                        sourceRef = json.optString("sourceRef").takeIf { it.isNotEmpty() },
                        publishedDate = json.optString("publishedDate").takeIf { it.isNotEmpty() },
                        lastReviewed = json.optString("lastReviewed").takeIf { it.isNotEmpty() },
                        nextReview = json.optString("nextReview").takeIf { it.isNotEmpty() },
                        version = json.optInt("version", 1),
                        rightsStatus = json.optString("rightsStatus", "pending"),
                        isBuiltin = true
                    )
                )
            }
        }
    }
}
