package com.rescuedesk.poc.engine

import java.io.File

/**
 * Candidate runtime #2 (docs/PHASE1-MODEL-POC.md §2.1): llama.cpp with GGUF
 * models (Qwen3-0.6B Q4 primary, Gemma 3 1B port secondary).
 *
 * Wiring checklist — execute on the Week-1 spike device:
 *  1. Build libllama.so + a thin JNI wrapper (prebuilt from the pinned llama.cpp
 *     commit, or via the community android binding). Record commit hash in §7.
 *  2. Push artifact: adb push qwen3-0.6b-q4_k_m.gguf
 *     /sdcard/Android/data/com.rescuedesk.poc/files/models/
 *  3. llama_model_load with n_gpu_layers = 0 first (CPU baseline for comparability),
 *     then try Vulkan offload as an extra row in the matrix.
 *  4. generate(): same buildPrompt() input as LiteRtEngine so quality is
 *     compared apples-to-apples; stream tokens via onToken; stop at EOS or 256
 *     tokens; apply the same citation-validator rule.
 *  5. unload(): llama_free model + context, verify RSS baseline (§5.4).
 */
class LlamaCppEngine(private val modelFile: File) : PromptedEngine(modelFile.absolutePath) {

    override suspend fun ensureLoaded(): Result<Unit> = runCatching {
        _status.value = EngineStatus.Loading
        require(modelFile.exists()) {
            "Model missing: ${modelFile.path} — adb push the .gguf artifact first (§5.1)"
        }
        // TODO(spike): System.loadLibrary("llama_jni"); nativeInit(modelPath)
        _status.value = EngineStatus.Failed
        throw IllegalStateException("llama.cpp JNI binding not yet wired in this spike build")
    }

    override suspend fun generate(
        question: String,
        contextChunks: List<String>,
        onToken: (String) -> Unit
    ): Result<GroundedAnswer> = runCatching {
        check(_status.value == EngineStatus.Ready) { "Engine not loaded" }
        // TODO(spike): nativeGenerate(buildPrompt(...)) { token -> onToken(token) }
        error("llama.cpp JNI binding not yet wired in this spike build")
    }

    override fun unload() {
        // TODO(spike): nativeFree() — llama_free(model), llama_free(ctx)
        super.unload()
    }
}
