package com.rescuedesk.ai.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One emergency guide item. Carries the full PRD §8.2 content metadata —
 * stored explicitly, never display-derived.
 */
@Entity(
    tableName = "guides",
    indices = [
        Index(value = ["publicId", "language"], unique = true),
        Index(value = ["category"])
    ]
)
data class GuideEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "publicId") val publicId: String,
    val title: String,
    /** Category key, e.g. "typhoon", "flood", "earthquake", "fire". */
    val category: String,
    val summary: String,
    /** Structured guide body; numbered steps separated by newlines. */
    val body: String,
    /** "Avoid these actions" items separated by newlines (PRD §5.7 layout). */
    val avoid: String = "",
    /** BCP-47 tag: "en" or "fil". */
    val language: String,
    val sourceName: String,
    val sourceRef: String?,
    val publishedDate: String?,
    val lastReviewed: String?,
    val nextReview: String?,
    val version: Int = 1,
    /** "cleared" | "pending" | "restricted" — only cleared items may ship (PRD §10.3). */
    val rightsStatus: String = "pending",
    /** True for guides bundled in the APK (PRD §5.4 built-in minimum set). */
    val isBuiltin: Boolean = true,
    /** Version of the downloadable pack that superseded this row, 0 = built-in active. */
    val packVersion: Int = 0
)

/**
 * Local full-text index for guide search (PRD §5.9, FR-01).
 * FTS4 via Room is the safe default; evaluate an FTS5 custom SQLite build in
 * the Phase 1 PoC (docs/ARCHITECTURE.md §9.2). Index stays in sync with
 * [GuideEntity] through Room's content-sync support.
 */
@Fts4(contentEntity = GuideEntity::class)
@Entity(tableName = "guides_fts")
data class GuideFtsEntity(
    val title: String,
    val summary: String,
    val body: String
)
