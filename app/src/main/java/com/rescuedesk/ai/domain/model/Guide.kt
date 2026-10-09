package com.rescuedesk.ai.domain.model

/** Runtime/installation state of the on-device model (PRD §5.8 model readiness indicator). */
enum class ModelStatus { NotInstalled, Installing, Ready, Incompatible, Error }

/** A guide as the UI and retrieval layers consume it. */
data class Guide(
    val id: Long,
    val publicId: String,
    val title: String,
    val category: String,
    val summary: String,
    val body: String,
    val language: String,
    val sourceName: String,
    val lastReviewed: String?,
    val nextReview: String?,
    val version: Int,
    val rightsStatus: String,
    val isBuiltin: Boolean
) {
    /**
     * PRD §8.3 freshness threshold: warning shows when next_review_date has passed
     * or more than 12 months elapsed since the last approved review.
     * Flagged items stay readable but are excluded from AI grounding.
     */
    fun needsReview(todayIso: String): Boolean {
        nextReview?.let { if (it < todayIso) return true }
        val review = lastReviewed ?: return true
        // 12-month staleness check on ISO dates (yyyy-MM-dd); exact calendar math
        // belongs to the CheckFreshness use case once real dates flow.
        val reviewYear = review.take(4).toIntOrNull() ?: return true
        val todayYear = todayIso.take(4).toIntOrNull() ?: return true
        return todayYear - reviewYear >= 1
    }
}
