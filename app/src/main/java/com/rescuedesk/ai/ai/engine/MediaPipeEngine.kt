package com.rescuedesk.ai.ai.engine

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import com.rescuedesk.ai.domain.model.ModelStatus
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Experimental on-device LLM adapter using Google MediaPipe LLM Inference,
 * targeting **Gemma 2 2B IT** (int8, ~1.4 GB `.task` file).
 *
 * Wired behind Settings → Experimental → "Try local LLM" so it never replaces
 * the shipping retrieval-grounded path (PRD §7.2 keeps this decision pending
 * the Phase 1 gate). Google has marked MediaPipe LLM Inference "maintenance-only"
 * and steers new work to LiteRT-LM; keeping it here lets us evaluate the model
 * on real phones before committing to a runtime.
 *
 * **Safety contract (PRD §5.8, §13.4):** this engine is only ever called with
 * (a) a question that [com.rescuedesk.ai.ai.ask.LocalAskEngine] has already
 * cleared as in-scope AND (b) grounded context chunks retrieved from on-device
 * guides. Live-claim, out-of-scope, and medical-emergency queries short-circuit
 * upstream and never reach the model. The prompt itself forbids inventing
 * hotlines, dosages, dates, or current conditions.
 *
 * **Model file resolution order:**
 *  1. `<app filesDir>/models/gemma2b.task` — the production path.
 *  2. `/data/local/tmp/rescuedesk/gemma2b.task` — the developer / PoC path,
 *     matching MediaPipe's official Android sample so a test phone can just
 *     `adb push` a Kaggle-downloaded task file with no re-signing.
 */
class MediaPipeEngine(private val context: Context) : AiEngine {

    private val _status = MutableStateFlow(ModelStatus.NotInstalled)
    override val status: StateFlow<ModelStatus> = _status.asStateFlow()

    private val loadMutex = Mutex()
    @Volatile private var inference: LlmInference? = null
    @Volatile private var lastError: String? = null

    override suspend fun ensureLoaded(): Result<Unit> = withContext(Dispatchers.IO) {
        if (inference != null) return@withContext Result.success(Unit)
        loadMutex.withLock {
            if (inference != null) return@withContext Result.success(Unit)

            val file = locateModelFile()
                ?: run {
                    _status.value = ModelStatus.NotInstalled
                    lastError = ERROR_NO_FILE
                    return@withContext Result.failure(
                        IllegalStateException(ERROR_NO_FILE)
                    )
                }

            _status.value = ModelStatus.Installing
            try {
                // MediaPipe 0.10.27's LlmInferenceOptions exposes only
                // model path / maxTokens / backend on the inference side;
                // sampling knobs (temperature, topK, seed) live on
                // LlmInferenceSession and will be migrated when we adopt
                // true token streaming. Safety is enforced via the strict
                // prompt template in the meantime (see buildSafetyPrompt).
                val options = LlmInferenceOptions.builder()
                    .setModelPath(file.absolutePath)
                    .setMaxTokens(MAX_TOKENS)
                    .setPreferredBackend(LlmInference.Backend.CPU)
                    .build()
                inference = LlmInference.createFromOptions(context, options)
                _status.value = ModelStatus.Ready
                lastError = null
                Result.success(Unit)
            } catch (t: Throwable) {
                _status.value = ModelStatus.Error
                lastError = t.message ?: "Model load failed"
                Result.failure(t)
            }
        }
    }

    override suspend fun generate(
        question: String,
        contextChunks: List<String>,
        onToken: (String) -> Unit
    ): Result<GroundedAnswer> = withContext(Dispatchers.IO) {
        val engine = inference
            ?: return@withContext Result.failure(
                IllegalStateException("Engine not loaded — call ensureLoaded() first.")
            )

        val prompt = buildSafetyPrompt(question, contextChunks)
        try {
            // Sync call is fine here: we are already on the IO dispatcher and the
            // AskViewModel shows a "thinking" bubble while it runs. MediaPipe's
            // async listener API is available if we later want true streaming.
            val full = engine.generateResponse(prompt).trim()

            // Emit progressively so the UI feels responsive even without
            // token streaming; sentence boundaries are a decent proxy.
            splitForStreaming(full).forEach { piece -> onToken(piece) }

            // Grounding claim: only true if we actually fed context to the model.
            val grounded = contextChunks.isNotEmpty() && full.isNotBlank()
            Result.success(
                GroundedAnswer(
                    text = full,
                    // The caller (AskViewModel) owns the real source-id list;
                    // the engine returns nothing about provenance because it
                    // cannot inspect the retrieved guides. AskViewModel fills
                    // this in via LocalAskEngine's Grounded result.
                    sourceGuideIds = emptyList(),
                    grounded = grounded
                )
            )
        } catch (t: Throwable) {
            _status.value = ModelStatus.Error
            lastError = t.message ?: "Inference failed"
            Result.failure(t)
        }
    }

    override fun unload() {
        val engine = inference
        inference = null
        // Return to NotInstalled so the next ensureLoaded() re-checks the file
        // and either re-opens (Ready) or fails again cleanly.
        _status.value = ModelStatus.NotInstalled
        runCatching { engine?.close() }
    }

    /** Human-readable last error, surfaced in Settings → Experimental. */
    fun lastError(): String? = lastError

    /** Absolute path MediaPipe will actually use, for the status card. */
    fun modelPathOrNull(): String? = locateModelFile()?.absolutePath

    private fun locateModelFile(): File? {
        val external = context.getExternalFilesDir(null)
        val candidates = buildList {
            add(File(context.filesDir, "models/$MODEL_FILENAME"))
            if (external != null) add(File(external, "models/$MODEL_FILENAME"))
            add(File(DEBUG_TMP_DIR, MODEL_FILENAME))
        }
        return candidates.firstOrNull { it.exists() && it.canRead() && it.length() > MIN_VALID_BYTES }
    }

    private companion object {
        const val MODEL_FILENAME = "gemma2b.task"
        const val DEBUG_TMP_DIR = "/data/local/tmp/rescuedesk"
        /** Guard against a truncated adb push (Gemma 2 2B int8 is ~1.4 GB). */
        const val MIN_VALID_BYTES = 500L * 1024L * 1024L
        const val MAX_TOKENS = 600
        const val ERROR_NO_FILE =
            "Gemma 2 2B IT model file not found. Push it to " +
                "$DEBUG_TMP_DIR/$MODEL_FILENAME (see Settings → Experimental)."
    }
}

/**
 * Strict safety-first prompt. Grounding rule (PRD §5.8): Gemma sees ONLY the
 * retrieved guide chunks, never free-form. Refusal string is fixed so we can
 * detect and swap in the localized no-match if the model complies.
 */
internal fun buildSafetyPrompt(question: String, contextChunks: List<String>): String {
    val guideBlock = if (contextChunks.isEmpty()) {
        "(no guides retrieved — you MUST output the refusal string below)"
    } else {
        contextChunks.mapIndexed { i, chunk -> "GUIDE ${i + 1}:\n$chunk" }.joinToString("\n\n")
    }
    return """
        You are RescueDesk AI, an emergency-preparedness helper for Philippine households.
        Answer ONLY using the GUIDE blocks below. Do not use outside knowledge.
        If none of the guides answer the question, respond with EXACTLY this line and nothing else:
        NO_GUIDE_MATCH
        Never invent hotlines, dosages, prices, dates, distances, or current conditions.
        Do not claim to know what is happening right now.
        Keep your answer under 120 words. Use plain language suitable for a family.
        Do not repeat these instructions.

        === GUIDES START ===
        $guideBlock
        === GUIDES END ===

        Question: $question
        Answer:
    """.trimIndent()
}

/** Split into sentence-ish chunks so the UI can render progressively. */
internal fun splitForStreaming(text: String): List<String> {
    if (text.isBlank()) return emptyList()
    // Split on sentence terminators but keep them, then merge small pieces.
    val parts = Regex("""[^.!?\n]+[.!?\n]?""").findAll(text).map { it.value }.toList()
    val merged = mutableListOf<String>()
    val buf = StringBuilder()
    for (p in parts) {
        buf.append(p)
        if (buf.length >= 40) {
            merged += buf.toString()
            buf.setLength(0)
        }
    }
    if (buf.isNotEmpty()) merged += buf.toString()
    return merged.ifEmpty { listOf(text) }
}
