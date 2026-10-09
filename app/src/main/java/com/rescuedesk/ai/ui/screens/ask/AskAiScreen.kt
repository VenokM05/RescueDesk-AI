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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        Text("Ask RescueDesk AI", style = MaterialTheme.typography.displaySmall)

        if (modelStatus != ModelStatus.Ready) {
            // PRD §5.8 Fallback State
            Text(
                text = "Offline AI is not ready on this device.",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Emergency guides still work without it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onOpenGuides, modifier = Modifier.fillMaxWidth()) {
                Text("Open emergency guides")
            }
            OutlinedButton(onClick = { /* TODO Phase 3: retry AI setup flow */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Retry AI setup")
            }
        } else {
            // TODO Phase 3 (GO path): chat area, grounded answer structure
            // (short answer → steps → caution → sources), input field, send.
            Text("Chat UI arrives with the Phase 3 runtime integration.")
        }

        // Suggested chips are visible but disabled until the model is Ready (PRD §5.8).
        Text("Examples you can ask once AI is set up", style = MaterialTheme.typography.titleLarge)
        listOf(
            "What should I put in a go-bag?",
            "What should my family prepare before a typhoon?",
            "How can I prepare our house for an earthquake?"
        ).forEach { chip ->
            SuggestionChip(
                onClick = { },
                enabled = modelStatus == ModelStatus.Ready,
                label = { Text(chip) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
