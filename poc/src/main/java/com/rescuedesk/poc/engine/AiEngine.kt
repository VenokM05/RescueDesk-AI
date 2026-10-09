package com.rescuedesk.poc.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Grounded answer shape mirrored from com.rescuedesk.ai.ai.engine (app module).
 * The PoC deliberately does NOT depend on :app — copy the contract, not the
 * module, so the spike can never affect the release build
 * (docs/PHASE1-MODEL-POC.md section 5). Keep in sync manually; the app interface is
 * frozen for Phase 1.
 */
data class GroundedAnswer(
    val text: String,
    val sourceGuideIds: List<Long>,
    val grounded: Boolean
)

enum class EngineStatus { NotInstalled, Loading, Ready, Failed }

/** Mirror of the app's AiEngine interface (docs/ARCHITECTURE.md section 4). */
interface AiEngine {
    val status: StateFlow<EngineStatus>
    suspend fun ensureLoaded(): Result<Unit>
    suspend fun generate(
        question: String,
        contextChunks: List<String>,
        onToken: (String) -> Unit
    ): Result<GroundedAnswer>
    fun unload()
}

/**
 * Base for the two candidate adapters: shared status plumbing and a
 * well-formed prompt template so both runtimes see identical input (section 1:
 * grounding quality is compared across runtimes, so the prompt is fixed).
 */
abstract class PromptedEngine(private val modelPath: String) : AiEngine {

    protected val _status = MutableStateFlow(EngineStatus.NotInstalled)
    override val status: StateFlow<EngineStatus> = _status.asStateFlow()

    protected fun buildPrompt(question: String, contextChunks: List<String>): String = buildString {
        appendLine("You are a Philippine emergency-preparedness assistant.")
        appendLine("Answer ONLY from the GUIDE EXCERPTS below. If the answer is not in them,")
        appendLine("say you do not know and suggest checking official sources. Cite excerpts as [1], [2].")
        appendLine()
        appendLine("GUIDE EXCERPTS:")
        contextChunks.forEachIndexed { i, chunk -> appendLine("[${i + 1}] $chunk") }
        appendLine()
        appendLine("QUESTION: $question")
        appendLine("ANSWER:")
    }

    /** PRD architecture section 5.3: RSS must return to baseline after every run. */
    override fun unload() {
        _status.value = EngineStatus.NotInstalled
    }
}
