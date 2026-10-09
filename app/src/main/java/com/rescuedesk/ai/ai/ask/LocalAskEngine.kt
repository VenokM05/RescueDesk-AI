package com.rescuedesk.ai.ai.ask

import com.rescuedesk.ai.data.repository.GuideRepository
import kotlinx.coroutines.flow.first

/**
 * PRD §7.3 "start with local keyword retrieval" + §5.8 AI safety behavior.
 *
 * This is a genuine offline capability that needs NO model and NO network:
 * it retrieves from the on-device guide library (FTS) and composes an answer
 * strictly from that cleared guide text, so it is grounded by construction —
 * it cannot invent an instruction that is not in a guide. The generative
 * LLM path (Gemma / llama.cpp) remains a Phase 1 gate experiment in :poc;
 * this engine is what makes Screen H usable on a real phone today.
 *
 * Deliberately resource-free: it returns an [AskResult] signal, and the
 * ViewModel maps it to localized strings (PRD layering — domain/data never
 * touch R.*).
 */
class LocalAskEngine(private val guideRepository: GuideRepository) {

    sealed interface AskResult {
        /** A grounded answer drawn from [sourceIds] guides. */
        data class Grounded(
            val lead: String,
            val steps: List<String>,
            val caution: String?,
            val sourceIds: List<Long>
        ) : AskResult

        /** Question needs live conditions the app cannot know (PRD §5.8). */
        data object LiveRefusal : AskResult

        /** Question is outside emergency-preparedness scope (PRD §5.8). */
        data object ScopeRefusal : AskResult

        /** Nothing in the local library matched. */
        data object NoMatch : AskResult
    }

    suspend fun answer(question: String): AskResult {
        val q = question.lowercase()
        if (isLiveClaim(q)) return AskResult.LiveRefusal
        if (isOutOfScope(q)) return AskResult.ScopeRefusal

        val matches = guideRepository.searchGuides(question).first()
        if (matches.isEmpty()) return AskResult.NoMatch

        val best = matches.first()
        val lead = best.summary.ifBlank { best.title }
        val steps = best.steps.take(MAX_STEPS)
        val caution = best.avoidList.firstOrNull()
        val sourceIds = matches.map { it.id }.distinct().take(MAX_SOURCES)
        return AskResult.Grounded(
            lead = lead,
            steps = steps,
            caution = caution,
            sourceIds = sourceIds
        )
    }

    /**
     * Live-claim detection: the app has no internet, sensor, or clock-synced
     * bulletin, so anything asking about *current* conditions must refuse and
     * point to official channels. Keyed on explicit "now/today/right now"
     * style phrasing (EN + Filipino) so timeless "how do I prepare" questions
     * are NOT caught here.
     */
    private fun isLiveClaim(q: String): Boolean {
        val markers = listOf(
            "right now", "as of", "currently", "today", "tonight", "this morning",
            "latest", "is it raining", "will it rain", "is signal", "signal no.", "raised",
            "is the road", "road open", "open now", "aftershock", "happening now",
            "current status", "ngayon", "ngayong", "kasalukuyan", "ngayong gabi",
            "ngayong umaga", "anong oras", "umuuulan", "bumabagyo"
        )
        return markers.any { q.contains(it) }
    }

    /** Out-of-scope: not emergency preparedness (finance, fortune, creative, etc.). */
    private fun isOutOfScope(q: String): Boolean {
        val markers = listOf(
            "poem", "write a song", "story about", "invest", "investment", "stock",
            "crypto", "lottery", "horoscope", "cursed", "magic", "feng shui",
            "magkano ang ganti", "compensation", "insurance claim", "loan",
            "pautang", "swerte", "best brand", "review ng"
        )
        return markers.any { q.contains(it) }
    }

    private companion object {
        const val MAX_STEPS = 4
        const val MAX_SOURCES = 3
    }
}
