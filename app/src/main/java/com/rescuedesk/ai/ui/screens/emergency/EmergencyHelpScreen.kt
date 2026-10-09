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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rescuedesk.ai.R
import com.rescuedesk.ai.ui.theme.Amber

/**
 * Screen F — Emergency Help (PRD §5.6).
 * Direct taps only — no typing, no AI dependency, no confirmation dialogs
 * before instructions (PRD §5.6 requirements).
 */
@Composable
fun EmergencyHelpScreen(onBack: () -> Unit, onOpenCategory: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(stringResource(R.string.emergency_title), style = MaterialTheme.typography.displaySmall)

        // Standing safety note (PRD §5.6). Emergency numbers appear only when the
        // content pipeline records source + applicability + review date (§8.2) —
        // none are cleared yet, so no number is shown here.
        Text(
            text = stringResource(R.string.emergency_safety_note),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Amber
        )

        // Each situation routes straight to its guide category (PRD §5.6:
        // direct taps, no typing, no AI dependency).
        val situations = listOf(
            R.string.emergency_situation_flood to "flood",
            R.string.emergency_situation_typhoon to "typhoon",
            R.string.emergency_situation_earthquake to "earthquake",
            R.string.emergency_situation_fire to "fire",
            R.string.emergency_situation_injury to "firstaid",
            R.string.emergency_situation_other to ""
        )
        situations.forEach { (labelRes, category) ->
            Button(
                onClick = { onOpenCategory(category) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(labelRes), style = MaterialTheme.typography.labelLarge)
            }
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.common_back))
        }
    }
}
