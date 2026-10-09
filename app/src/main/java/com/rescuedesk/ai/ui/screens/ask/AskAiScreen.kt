package com.rescuedesk.ai.ui.screens.ask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rescuedesk.ai.R

/**
 * Screen H — Ask RescueDesk AI (PRD §5.8), running in the offline
 * retrieval-grounded local mode (docs/ARCHITECTURE.md §4, PRD §7.3). Answers
 * come only from the on-device guides, are cited, and refuse live/out-of-scope
 * requests. The generative-LLM path stays gated in :poc until Phase 1 GO.
 */
@Composable
fun AskAiScreen(
    onOpenGuides: () -> Unit,
    onOpenGuide: (Long) -> Unit,
    viewModel: AskViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }

    // Auto-scroll when a new turn lands.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Persistent honesty banner: local mode, grounded to guides (PRD §5.8).
        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
            Text(
                text = stringResource(R.string.ask_local_note),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                // Announce new assistant turns politely (PRD §4.6 TalkBack).
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                },
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            if (messages.isEmpty()) {
                item { EmptyState(onOpenGuides = onOpenGuides) }
            }
            items(messages, key = { it.id }) { message ->
                MessageRow(message = message, onOpenGuide = onOpenGuide)
            }
        }

        // Example chips (enabled — local mode answers these from the guides).
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(R.string.ask_example_1, R.string.ask_example_2, R.string.ask_example_3).forEach { chipRes ->
                val label = stringResource(chipRes)
                SuggestionChip(
                    onClick = { viewModel.send(label) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hoisted so the semantics lambda (non-composable) can capture it.
            val hint = stringResource(R.string.ask_hint)
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text(hint) },
                // Placeholder disappears once text is entered — pin a stable
                // field description for TalkBack instead (PRD §4.6).
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = hint },
                maxLines = 4
            )
            IconButton(
                onClick = {
                    viewModel.send(input)
                    input = ""
                },
                enabled = input.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.ask_send))
            }
        }
    }
}

@Composable
private fun EmptyState(onOpenGuides: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.ask_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.ask_examples_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        AssistChip(
            onClick = onOpenGuides,
            label = { Text(stringResource(R.string.ask_open_guides)) }
        )
    }
}

@Composable
private fun MessageRow(message: ChatMessage, onOpenGuide: (Long) -> Unit) {
    val isUser = message.fromUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier.widthIn(max = if (isUser) 300.dp else 340.dp)
        ) {
            if (isUser) {
                ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                    Text(
                        message.userText.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else if (message.pending) {
                Text(
                    stringResource(R.string.ask_thinking),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(4.dp)
                )
            } else {
                OutlinedCard(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Canned refusal / no-match copy.
                        message.cannedRes?.let { res ->
                            Text(stringResource(res), style = MaterialTheme.typography.bodyLarge)
                        }
                        // Grounded answer structure: lead → steps → caution → sources.
                        message.lead?.let {
                            Text(it, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }
                        if (message.steps.isNotEmpty()) {
                            Text(
                                stringResource(R.string.detail_do_this_first),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            message.steps.forEachIndexed { i, step ->
                                Text("${i + 1}. $step", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        message.caution?.let {
                            Text(
                                stringResource(R.string.ask_caution),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (message.sources.isNotEmpty()) {
                            Text(
                                stringResource(R.string.ask_sources),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                message.sources.forEach { source ->
                                    AssistChip(
                                        onClick = { onOpenGuide(source.id) },
                                        label = { Text(source.title, style = MaterialTheme.typography.labelSmall) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.MenuBook,
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
