package com.rescuedesk.ai.data.pack

import org.json.JSONObject

/**
 * Guide-pack manifest (docs/ARCHITECTURE.md §7). Served as plain JSON from a
 * first-party static endpoint — no backend service required at runtime.
 *
 * Schema (schemaVersion 1):
 * ```
 * { "schemaVersion": 1, "generatedAt": "2026-10-01",
 *   "guides": [ { "publicId", "language", "version", "url", "sha256" } ] }
 * ```
 * Each `url` points to a guide JSON with the same shape as the built-in
 * asset guides (steps[], avoid[], full PRD §8.2 metadata).
 */
data class PackGuideRef(
    val publicId: String,
    val language: String,
    val version: Int,
    val url: String,
    val sha256: String
)

data class PackManifest(
    val generatedAt: String,
    val guides: List<PackGuideRef>
) {
    companion object {
        const val SCHEMA_VERSION = 1

        /** Throws on malformed or unsupported manifests — callers treat those as Failed. */
        fun parse(json: JSONObject): PackManifest {
            val schema = json.optInt("schemaVersion", -1)
            require(schema == SCHEMA_VERSION) { "Unsupported pack schemaVersion $schema" }
            val arr = json.getJSONArray("guides")
            val refs = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                PackGuideRef(
                    publicId = o.getString("publicId"),
                    language = o.getString("language"),
                    version = o.getInt("version"),
                    url = o.getString("url"),
                    sha256 = o.getString("sha256").lowercase()
                )
            }
            // Duplicate identities would silently drop content during activation.
            val dupes = refs.groupBy { "${it.publicId}-${it.language}" }.filterValues { it.size > 1 }
            require(dupes.isEmpty()) { "Manifest has duplicate guide identities: ${dupes.keys}" }
            return PackManifest(json.optString("generatedAt"), refs)
        }
    }
}

/** Sealed sync status consumed by Screen L and the worker. */
sealed interface PackStatus {
    data object Idle : PackStatus
    data object Checking : PackStatus
    data class Downloading(val done: Int, val total: Int) : PackStatus
    data object Applying : PackStatus
    data class UpToDate(val downloaded: Int, val skipped: Int) : PackStatus
    data class Failed(val reason: String) : PackStatus
}
