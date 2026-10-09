package com.rescuedesk.ai.ai.engine

import com.rescuedesk.ai.domain.model.ModelStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * An answer produced by the on-device model, grounded in retrieved local chunks.
 * [sourceGuideIds] must always be a subset of the guides actually retrieved for
 * the prompt — the safety layer rejects anything else (PRD FR-04, docs/ARCHITECTURE.md section 5.2).
 */
data class GroundedAnswer(
    val text: String,
    val sourceGuideIds: List<Long>,
    val grounded: Boolean
)

/**
 * Abstraction over the Phase 1 runtime decision (PRD section 7.2). Only the concrete
 * adapter may touch native inference libraries; UI, retrieval, and safety code
 * stay engine-agnostic so the guide-only NO-GO path requires zero rework.
 */
interface AiEngine {
    val status: StateFlow<ModelStatus>
    suspend fun ensureLoaded(): Result<Unit>
    suspend fun generate(
        question: String,
        contextChunks: List<String>,
        onToken: (String) -> Unit
    ): Result<GroundedAnswer>
    fun unload()
}

/**
 * Default scaffold implementation: no runtime bundled yet. Renders the PRD section 5.8
 * fallback state wherever Ask Juan is surfaced.
 */
class UnavailableAiEngine : AiEngine {

    private val _status = MutableStateFlow(ModelStatus.NotInstalled)
    override val status: StateFlow<ModelStatus> = _status.asStateFlow()

    override suspend fun ensureLoaded(): Result<Unit> =
        Result.failure(IllegalStateException("No inference runtime bundled (awaiting Phase 1 gate)."))

    override suspend fun generate(
        question: String,
        contextChunks: List<String>,
        onToken: (String) -> Unit
    ): Result<GroundedAnswer> =
        Result.failure(IllegalStateException("Offline AI is not ready on this device."))

    override fun unload() = Unit
}
