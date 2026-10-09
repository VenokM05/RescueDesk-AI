package com.rescuedesk.ai.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rescuedesk.ai.R
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.data.local.AppLanguage
import com.rescuedesk.ai.data.local.TextSize
import com.rescuedesk.ai.ui.labelRes
import kotlinx.coroutines.launch

/**
 * Screen M — Settings (PRD §5.13), minimal Phase 2 slice: language, text size,
 * and the privacy posture statement. Go-bag, plan export, and data deletion
 * arrive with Phase 4.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenOffline: () -> Unit) {
    val store = ServiceLocator.preferencesStore
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.displaySmall)

        val current = settings ?: return@Column

        SectionCard(stringResource(R.string.settings_language_section)) {
            AppLanguage.entries.forEach { lang ->
                SettingOption(
                    label = lang.label,
                    selected = current.language == lang,
                    onClick = { scope.launch { store.setLanguage(lang) } }
                )
            }
            SettingOption(
                label = stringResource(R.string.settings_language_follow),
                selected = current.language == null,
                onClick = { scope.launch { store.setLanguage(null) } }
            )
        }

        SectionCard(stringResource(R.string.settings_textsize_section)) {
            TextSize.entries.forEach { size ->
                SettingOption(
                    label = stringResource(size.labelRes()),
                    selected = current.textSize == size,
                    onClick = { scope.launch { store.setTextSize(size) } }
                )
            }
        }

        SectionCard(stringResource(R.string.settings_offline_section)) {
            Text(
                stringResource(R.string.settings_offline_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onOpenOffline) {
                Text(stringResource(R.string.settings_offline_open))
            }
        }

        SectionCard(stringResource(R.string.settings_privacy_section)) {
            Text(
                stringResource(R.string.settings_privacy_body),
                style = MaterialTheme.typography.bodyLarge
            )
            // TODO Phase 4: "Delete my data" (household plan, contacts, chat).
        }

        SectionCard(stringResource(R.string.settings_about_section)) {
            Text(
                stringResource(R.string.settings_about_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun SettingOption(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = (if (selected) "✓ " else "") + label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
