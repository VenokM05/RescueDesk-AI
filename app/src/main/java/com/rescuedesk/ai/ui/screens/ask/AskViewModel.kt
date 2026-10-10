package com.rescuedesk.ai.ui.screens.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuedesk.ai.R
import com.rescuedesk.ai.ai.ask.LocalAskEngine
import com.rescuedesk.ai.ai.ask.LocalAskEngine.AskResult
import com.rescuedesk.ai.ai.engine.MediaPipeEngine
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.domain.model.ModelStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SourceRef(val id: Long, val title: String)

/**
 * One turn on Screen H. Canned assistant copy (refusals / no-match) is carried
 * as a string-resource id so it follows the in-app language at composition
 * time; guide-derived text ([lead], [steps], [caution], [sources]) already
 * arrives in the localized guide language.
 *
 * When the experimental on-device LLM is enabled AND successfully produced an
 * answer, [llmText] carries its free-form output. The UI then renders that
 * single paragraph instead of the composed lead/steps/caution structure, but
 * still shows the [sources] chips so the user can always verify. If the LLM
 * errors or refuses, [llmText] stays null and the composed path is used —
 * the feature degrades gracefully.
 */
data class ChatMessage(
    val id: Long,
    val fromUser: Boolean,
    val userText: String? = null,
    val cannedRes: Int? = null,
    val lead: String? = null,
    val steps: List<String> = emptyList(),
    val caution: String? = null,
    val sources: List<SourceRef> = emptyList(),
    val llmText: String? = null,
    val pending: Boolean = false
)

class AskViewModel : ViewModel() {

    private val engine = LocalAskEngine(ServiceLocator.guideRepository)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private var nextId = 0L

    fun send(rawQuestion: String) {
        val question = rawQuestion.trim()
        if (question.isEmpty()) return
        _messages.value += ChatMessage(id = nextId++, fromUser = true, userText = question)

        val placeholderId = nextId++
        _messages.value += ChatMessage(id = placeholderId, fromUser = false, pending = true)

        viewModelScope.launch {
            // Brief beat so the "Searching the guides…" state is visible.
            delay(350)
            val result = engine.answer(question)
            val answer = when (result) {
                is AskResult.Grounded -> groundedMessage(placeholderId, result)
                AskResult.LiveRefusal -> ChatMessage(placeholderId, false, cannedRes = R.string.ask_live_refusal)
                AskResult.ScopeRefusal -> ChatMessage(placeholderId, false, cannedRes = R.string.ask_scope_refusal)
                AskResult.MedicalEscalation -> ChatMessage(placeholderId, false, cannedRes = R.string.ask_medical_note)
                // No dead-end: pair the short not-found note with the actual
                // guides installed on this device as tappable source chips.
                is AskResult.NoMatch -> ChatMessage(
                    id = placeholderId,
                    fromUser = false,
                    cannedRes = R.string.ask_nomatch,
                    sources = result.topics.map { (id, title) -> SourceRef(id, title) }
                )
            }
            _messages.value = _messages.value.map { if (it.id == placeholderId) answer else it }

            // Optional experimental LLM phrasing pass. Only reached for a
            // Grounded answer; refusal / medical / no-match are never fed to
            // the model. On any failure we keep the composed answer as-is.
            if (result is AskResult.Grounded) {
                maybeLlmPolish(placeholderId, question, result)
            }
        }
    }

    private suspend fun groundedMessage(
        messageId: Long,
        result: AskResult.Grounded
    ): ChatMessage {
        // Resolve guide ids to display titles; skip any guide that vanished.
        val sources = result.sourceIds.mapNotNull { id ->
            ServiceLocator.guideRepository.guideById(id)?.let { SourceRef(it.id, it.title) }
        }
        return ChatMessage(
            id = messageId,
            fromUser = false,
            lead = result.lead,
            steps = result.steps,
            caution = result.caution,
            sources = sources
        )
    }

    /**
     * If the user turned on Settings → Experimental → "Try local LLM" AND the
     * MediaPipe engine loaded successfully, re-phrase the grounded content as
     * a single flowing answer. Sources chips remain attached so verification
     * is one tap away. Any error → keep the composed answer silently.
     */
    private suspend fun maybeLlmPolish(
        messageId: Long,
        question: String,
        result: AskResult.Grounded
    ) {
        val enabled = runCatching {
            // Read current settings once; do NOT pass a predicate to first()
            // — that would suspend until some future emission matches.
            ServiceLocator.preferencesStore.settings.first().llmEnabled
        }.getOrDefault(false)
        val llm: MediaPipeEngine? = ServiceLocator.mediaPipeEngine
        android.util.Log.i(
            "LLM-ask",
            "polish requested: enabled=$enabled enginePresent=${llm != null} status=${llm?.status?.value}"
        )
        if (!enabled) return
        llm ?: return

        // Load lazily; if this is the first try and it fails, surface NotInstalled
        // in Settings / Offline and stay on the composed path.
        if (llm.status.value != ModelStatus.Ready) {
            val loaded = llm.ensureLoaded()
            if (loaded.isFailure) {
                android.util.Log.w(
                    "LLM-ask",
                    "ensureLoaded FAILED — staying on composed answer: ${loaded.exceptionOrNull()?.message}"
                )
                return
            }
        }

        // Feed the same guide content we already retrieved — no new search.
        // Name the real source guide so the model rephrases in the right topic
        // frame instead of seeing a generic placeholder title.
        val primaryTitle = result.sourceIds.firstNotNullOfOrNull {
            ServiceLocator.guideRepository.guideById(it)?.title
        } ?: "local emergency guide"
        android.util.Log.i("LLM-ask", "grounding on guide: $primaryTitle")
        val chunks = buildList {
            val primary = buildString {
                appendLine("Title: $primaryTitle")
                appendLine("Summary: ${result.lead}")
                if (result.steps.isNotEmpty()) {
                    appendLine("Steps:")
                    result.steps.forEachIndexed { i, s -> appendLine("${i + 1}. $s") }
                }
                result.caution?.let { appendLine("Avoid: $it") }
            }
            add(primary)
        }

        val startedAt = android.os.SystemClock.elapsedRealtime()
        val gen = llm.generate(question, chunks) { /* partial token; ignore for now */ }
        val elapsed = android.os.SystemClock.elapsedRealtime() - startedAt
        val answer = gen.getOrNull()
        if (answer == null) {
            android.util.Log.w(
                "LLM-ask",
                "generate FAILED after ${elapsed} ms — staying on composed answer: ${gen.exceptionOrNull()?.message}"
            )
            return
        }
        android.util.Log.i("LLM-ask", "generate ok in ${elapsed} ms, ${answer.text.length} chars, grounded=${answer.grounded}")
        if (answer.text.isBlank()) return
        // Honour the model's refusal token — swap in the localized no-match.
        if (answer.text.contains("NO_GUIDE_MATCH", ignoreCase = true)) {
            android.util.Log.i("LLM-ask", "model returned NO_GUIDE_MATCH — keeping composed answer")
            return
        }
        if (!answer.grounded) return

        // Replace the composed message body with the LLM text, keep sources.
        val existing = _messages.value.firstOrNull { it.id == messageId } ?: return
        val updated = existing.copy(
            lead = null,
            steps = emptyList(),
            caution = null,
            llmText = answer.text
        )
        _messages.value = _messages.value.map { if (it.id == messageId) updated else it }
    }
}
