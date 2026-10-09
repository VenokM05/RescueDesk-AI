package com.rescuedesk.ai.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rescuedesk.ai.R
import com.rescuedesk.ai.ui.theme.DarkRed

/**
 * Screen E — Home Dashboard (PRD §5.5).
 * Placeholder layout honoring the hierarchy rules: Emergency Help dominates;
 * guides are reachable without opening AI; the screen is not a dashboard of analytics.
 */
@Composable
fun HomeScreen(
    onEmergencyHelp: () -> Unit,
    onOpenGuides: () -> Unit,
    onOpenCategory: (String) -> Unit,
    onAskAi: () -> Unit,
    onMyFamily: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Brand lockup (PRD §5.5 top section): logo + accessible name label.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.logo_horizontal),
                contentDescription = "RescueDesk AI",
                modifier = Modifier
                    .height(44.dp)
                    .width(132.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings / Mga setting",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            text = "Kumusta! Handa ka na ba?",
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = "Your emergency guides are ready when you need them.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Primary emergency action — must stay more prominent than optional features (PRD §5.5).
        Button(
            onClick = onEmergencyHelp,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkRed)
        ) {
            Text("Emergency Help", style = MaterialTheme.typography.labelLarge)
        }

        // Quick-access category cards (PRD §5.5): route straight to the category's guides.
        Text("Quick guides", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickCard("Bagyo at Baha", "Typhoon and flood", Modifier.weight(1f)) { onOpenCategory("typhoon") }
            QuickCard("Lindol", "Earthquake", Modifier.weight(1f)) { onOpenCategory("earthquake") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickCard("Sunog", "Fire", Modifier.weight(1f)) { onOpenCategory("fire") }
            QuickCard("First Aid", "Basic first aid", Modifier.weight(1f)) { onOpenCategory("firstaid") }
        }

        Spacer(Modifier.height(4.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("My Family Plan", style = MaterialTheme.typography.titleLarge)
                Text("Not started yet", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                SuggestionChip(onClick = onMyFamily, label = { Text("Continue plan") })
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("May tanong ka?", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                SuggestionChip(onClick = onAskAi, label = { Text("Ask RescueDesk AI") })
            }
        }

        // Offline status strip (PRD §5.5). TODO Phase 3: bind real model/pack state.
        Text(
            text = "Offline: built-in emergency guides available · AI model not installed",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickCard(title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
