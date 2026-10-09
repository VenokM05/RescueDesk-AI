package com.rescuedesk.ai.ui.screens.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuedesk.ai.R
import com.rescuedesk.ai.ai.ask.LocalAskEngine
import com.rescuedesk.ai.ai.ask.LocalAskEngine.AskResult
import com.rescuedesk.ai.app.ServiceLocator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SourceRef(val id: Long, val title: String)

/**
 * One turn on Screen H. Canned assistant copy (refusals / no-match) is carried
 * as a string-resource id so it follows the in-app language at composition
 * time; guide-derived text ([lead], [steps], [caution], [sources]) already
 * arrives in the localized guide language.
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
                AskResult.NoMatch -> ChatMessage(placeholderId, false, cannedRes = R.string.ask_nomatch)
            }
            _messages.value = _messages.value.map { if (it.id == placeholderId) answer else it }
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
}
