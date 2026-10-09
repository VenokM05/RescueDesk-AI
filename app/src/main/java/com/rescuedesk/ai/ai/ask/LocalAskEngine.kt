package com.rescuedesk.ai.ai.ask

import com.rescuedesk.ai.data.repository.GuideRepository
import kotlinx.coroutines.flow.first

/**
 * PRD section 7.3 "start with local keyword retrieval" + section 5.8 AI safety behavior.
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

        /** Question needs live conditions the app cannot know (PRD section 5.8). */
        data object LiveRefusal : AskResult

        /** Question is outside emergency-preparedness scope (PRD section 5.8). */
        data object ScopeRefusal : AskResult

        /**
         * A possible medical emergency. The app holds no first-aid/clinical
         * content, so it must escalate to professional help rather than answer
         * or return a generic no-match (PRD section 5.8 first-aid scope + section 13.4).
         */
        data object MedicalEscalation : AskResult

        /**
         * Nothing in the local library matched directly. Instead of a dead-end
         * canned line, carries the REAL topics this device can answer right now
         * (id + title of actual installed guides, in the user's language), so
         * the UI can offer tappable wayfinding into the guide library.
         */
        data class NoMatch(val topics: List<Pair<Long, String>>) : AskResult
    }

    suspend fun answer(question: String): AskResult {
        val q = question.lowercase()
        // Order matters: a live or medical emergency outranks everything else;
        // an out-of-scope question must not be misread as a grounded request.
        if (isLiveClaim(q)) return AskResult.LiveRefusal
        if (isMedicalEmergency(q)) return AskResult.MedicalEscalation
        if (isOutOfScope(q)) return AskResult.ScopeRefusal

        val matches = guideRepository.searchGuides(question).first()
        if (matches.isEmpty()) {
            // Not a dead end: show what the library genuinely covers.
            val topics = runCatching {
                guideRepository.observeGuides().first()
                    .take(MAX_TOPIC_FALLBACK)
                    .map { it.id to it.title }
            }.getOrDefault(emptyList())
            return AskResult.NoMatch(topics)
        }

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
            "latest", "is it raining", "will it rain", "is signal", "signal no",
            "signal number", "storm signal", "is the road", "road open", "open now",
            "aftershock", "happening now", "current status", "live status",
            "ngayon", "ngayong", "kasalukuyan", "ngayong gabi", "ngayong umaga",
            "anong oras", "umuuulan", "bumabagyo"
        )
        return markers.any { q.contains(it) }
    }

    /**
     * Possible medical emergency presenters. Keyed on symptom/urgency wording
     * (not the word "first aid", which is a legitimate preparedness topic) so
     * we escalate real emergencies to professional care instead of answering
     * from general guides. Deliberately conservative — over-escalating is the
     * safe direction (PRD section 13.4).
     */
    private fun isMedicalEmergency(q: String): Boolean {
        val markers = listOf(
            "bleed", "won't stop bleeding", "unconscious", "not breathing",
            "can't breathe", "difficulty breathing", "seizure", "convulsion",
            "chest pain", "choking", "broken bone", "fracture", "snake bite",
            "spider bite", "dog bite", "kagat ng aso", "drowning", "drown",
            "second degree burn", "third degree burn", "lagnat", "fever",
            "pangingilabot", "namamaga", "gamot sa", "may buntis", "labor"
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
        // Cap the "I can help with…" wayfinding list so the bubble stays scannable.
        const val MAX_TOPIC_FALLBACK = 6
    }
}
