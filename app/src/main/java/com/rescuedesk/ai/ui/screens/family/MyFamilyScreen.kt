package com.rescuedesk.ai.ui.screens.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Screen J — My Family (PRD §5.10).
 * Scaffold placeholder listing the five sections. Phase 4 implements the
 * five-step wizard with per-step autosave (FR-05) and local-only storage.
 */
@Composable
fun MyFamilyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("My Family", style = MaterialTheme.typography.displaySmall)
        Text(
            text = "Everything you save here stays on this device unless you choose to share it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val sections = listOf(
            "Family Emergency Plan",
            "Emergency Contacts",
            "Meeting Places",
            "Go-Bag Checklist",
            "Important Reminders"
        )
        sections.forEach { label ->
            Button(
                onClick = { /* TODO Phase 4: open section wizard */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
