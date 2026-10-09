package com.rescuedesk.poc.engine

import java.io.File

/**
 * Candidate runtime #1 (docs/PHASE1-MODEL-POC.md section 2.1): LiteRT-LM via the
 * MediaPipe LLM Inference API (com.google.ai.edge.litertlm + LlmInference).
 *
 * Wiring checklist — execute on the Week-1 spike device:
 *  1. Add deps to poc/build.gradle.kts:
 *       implementation("com.google.ai.edge.litertlm:litertlm-android:<pinned>")
 *       implementation("com.google.mediapipe:tasks-genai:<pinned>")
 *     Pin the exact versions used; record them in the section 7 results sheet.
 *  2. Push artifact: adb push gemma3_1b-it-int4.task
 *     /sdcard/Android/data/com.rescuedesk.poc/files/models/
 *  3. Build LlmInference with LlmInferenceOptions(modelPath, maxTokens = 256,
 *     temperature = 0.2) — low temp for grounding (section 1: rephraser, not chatbot).
 *  4. generate(): feed buildPrompt() output to sendStream(), relay tokens via
 *     onToken, and flip grounded = true only if the answer cites at least one
 *     [n] marker present in the supplied excerpts (citation validator section 1).
 *  5. unload(): close the LlmInference instance, then verify RSS returns to
 *     baseline (section 5.4) — record the delta, do not trust it.
 */
class LiteRtEngine(private val modelFile: File) : PromptedEngine(modelFile.absolutePath) {

    override suspend fun ensureLoaded(): Result<Unit> = runCatching {
        _status.value = EngineStatus.Loading
        require(modelFile.exists()) {
            "Model missing: ${modelFile.path} — adb push the .task artifact first (section 5.1)"
        }
        // TODO(spike): LlmInference.createFromOptions(context, options)
        _status.value = EngineStatus.Failed
        throw IllegalStateException("LiteRT-LM binding not yet wired in this spike build")
    }

    override suspend fun generate(
        question: String,
        contextChunks: List<String>,
        onToken: (String) -> Unit
    ): Result<GroundedAnswer> = runCatching {
        check(_status.value == EngineStatus.Ready) { "Engine not loaded" }
        // TODO(spike): sendStream(buildPrompt(question, contextChunks), onToken)
        error("LiteRT-LM binding not yet wired in this spike build")
    }

    override fun unload() {
        // TODO(spike): close LlmInference, free native arena
        super.unload()
    }
}
