package com.rescuedesk.ai.ui.screens.emergency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rescuedesk.ai.ui.theme.Amber

/**
 * Screen F — Emergency Help (PRD §5.6).
 * Direct taps only — no typing, no AI dependency, no confirmation dialogs
 * before instructions (PRD §5.6 requirements).
 */
@Composable
fun EmergencyHelpScreen(onBack: () -> Unit, onOpenGuides: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("What is happening?", style = MaterialTheme.typography.displaySmall)

        // Standing safety note (PRD §5.6). Emergency numbers appear only when the
        // content pipeline records source + applicability + review date (§8.2) —
        // none are cleared yet, so no number is shown here.
        Text(
            text = "If you are in immediate danger, move to safety if possible and contact local emergency services.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Amber
        )

        val situations = listOf(
            "Flooding or rising water",
            "Typhoon or strong winds",
            "Earthquake",
            "Fire or smoke",
            "Injury or medical emergency",
            "Other emergency"
        )
        // TODO Phase 2: route each to its guide (Guide Detail), not the library root.
        situations.forEach { label ->
            Button(
                onClick = onOpenGuides,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
