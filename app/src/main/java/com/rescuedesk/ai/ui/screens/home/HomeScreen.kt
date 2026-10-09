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
import androidx.annotation.StringRes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
                    contentDescription = stringResource(R.string.home_settings_desc),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            text = stringResource(R.string.home_greeting),
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = stringResource(R.string.home_subtitle),
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
            Text(stringResource(R.string.emergency_help), style = MaterialTheme.typography.labelLarge)
        }

        // Quick-access category cards (PRD §5.5): route straight to the category's guides.
        Text(stringResource(R.string.home_quick_guides), style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickCard(
                R.string.home_cat_typhoon,
                R.string.home_cat_typhoon_sub,
                Modifier.weight(1f)
            ) { onOpenCategory("typhoon") }
            QuickCard(
                R.string.home_cat_earthquake,
                R.string.home_cat_earthquake_sub,
                Modifier.weight(1f)
            ) { onOpenCategory("earthquake") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickCard(R.string.home_cat_fire, R.string.home_cat_fire_sub, Modifier.weight(1f)) {
                onOpenCategory("fire")
            }
            QuickCard(
                R.string.home_cat_firstaid,
                R.string.home_cat_firstaid_sub,
                Modifier.weight(1f)
            ) { onOpenCategory("firstaid") }
        }

        Spacer(Modifier.height(4.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.home_plan_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.home_plan_not_started),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                SuggestionChip(
                    onClick = onMyFamily,
                    label = { Text(stringResource(R.string.home_plan_continue)) }
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.home_ask_title), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                SuggestionChip(
                    onClick = onAskAi,
                    label = { Text(stringResource(R.string.home_ask_chip)) }
                )
            }
        }

        // Offline status strip (PRD §5.5). TODO Phase 3: bind real model/pack state.
        Text(
            text = stringResource(R.string.home_offline_strip),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickCard(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(subtitle), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
