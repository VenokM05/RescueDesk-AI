package com.rescuedesk.ai.ui.screens.ask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rescuedesk.ai.R
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.domain.model.ModelStatus

/**
 * Screen H — Ask RescueDesk AI (PRD §5.8).
 * The scaffold ships only the fallback state, which is the required behavior
 * whenever the model is missing/incompatible: no blank chat, no endless loading.
 * Chat UI, suggested chips wiring, and source cards land in Phase 3 (GO path).
 */
@Composable
fun AskAiScreen(onOpenGuides: () -> Unit) {
    val modelStatus by ServiceLocator.aiEngine.status.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(stringResource(R.string.ask_title), style = MaterialTheme.typography.displaySmall)

        if (modelStatus != ModelStatus.Ready) {
            // PRD §5.8 Fallback State
            Text(
                text = stringResource(R.string.ask_not_ready),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.ask_not_ready_sub),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onOpenGuides, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.ask_open_guides))
            }
            OutlinedButton(onClick = { /* TODO Phase 3: retry AI setup flow */ }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.ask_retry_setup))
            }
        } else {
            // TODO Phase 3 (GO path): chat area, grounded answer structure
            // (short answer → steps → caution → sources), input field, send.
            Text(stringResource(R.string.ask_chat_placeholder))
        }

        // Suggested chips are visible but disabled until the model is Ready (PRD §5.8).
        Text(
            stringResource(R.string.ask_examples_title),
            style = MaterialTheme.typography.titleLarge
        )
        listOf(
            R.string.ask_example_1,
            R.string.ask_example_2,
            R.string.ask_example_3
        ).forEach { chipRes ->
            SuggestionChip(
                onClick = { },
                enabled = modelStatus == ModelStatus.Ready,
                label = { Text(stringResource(chipRes)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
