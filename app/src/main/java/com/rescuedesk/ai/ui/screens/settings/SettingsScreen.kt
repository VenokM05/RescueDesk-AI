package com.rescuedesk.ai.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
 * Screen M — Settings (PRD section 5.13): language, text size, offline manager
 * entry, privacy posture, and the FR-07 "delete all locally saved personal
 * information" tool (Phase 4, PRD section 10.1 data-subject-rights entry point).
 */
@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenOffline: () -> Unit) {
    val store = ServiceLocator.preferencesStore
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPrivacyNotice by remember { mutableStateOf(false) }

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
            // Destructive actions must be confirmed (PRD section 4.4); the dialog names
            // exactly what is wiped and what survives.
            TextButton(onClick = { showDeleteDialog = true }) {
                Text(
                    stringResource(R.string.settings_delete_all_button),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            // Plain-language privacy notice in the current language (PRD section 10.1).
            // Chevron gives a visual disclosure affordance; TalkBack still reads
            // the label alone (icon is decorative).
            TextButton(onClick = { showPrivacyNotice = !showPrivacyNotice }) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(stringResource(R.string.settings_privacy_notice_title))
                    Icon(
                        imageVector = if (showPrivacyNotice) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = androidx.compose.ui.Modifier.padding(start = 6.dp)
                    )
                }
            }
            if (showPrivacyNotice) {
                Text(
                    stringResource(R.string.settings_privacy_notice_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SectionCard(stringResource(R.string.settings_about_section)) {
            // Mission statement — why the project exists (user-facing, PRD section 2.1 spirit).
            Text(
                stringResource(R.string.settings_about_mission),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                stringResource(R.string.settings_about_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Author / team credit.
            Text(
                stringResource(R.string.settings_about_author),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Experimental on-device LLM toggle (Phase 1 candidate evaluation).
        // Off by default so the shipping retrieval-grounded path is unaffected.
        val llmOn = current.llmEnabled
        val llmEngine = ServiceLocator.mediaPipeEngine
        val llmStatus by (llmEngine?.status ?: kotlinx.coroutines.flow.MutableStateFlow(
            com.rescuedesk.ai.domain.model.ModelStatus.NotInstalled
        )).collectAsStateWithLifecycle(initialValue = com.rescuedesk.ai.domain.model.ModelStatus.NotInstalled)
        SectionCard(stringResource(R.string.settings_llm_section)) {
            Text(
                stringResource(R.string.settings_llm_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.settings_llm_toggle_label),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = llmOn,
                    onCheckedChange = { enabled ->
                        scope.launch { store.setLlmEnabled(enabled) }
                        // Try to load immediately when turning on so the
                        // status line reflects reality by the next frame.
                        if (enabled) scope.launch { llmEngine?.ensureLoaded() }
                        else llmEngine?.unload()
                    }
                )
            }
            val statusText = when (llmStatus) {
                com.rescuedesk.ai.domain.model.ModelStatus.NotInstalled ->
                    stringResource(R.string.settings_llm_status_not_installed)
                com.rescuedesk.ai.domain.model.ModelStatus.Installing ->
                    stringResource(R.string.settings_llm_status_loading)
                com.rescuedesk.ai.domain.model.ModelStatus.Ready ->
                    stringResource(R.string.settings_llm_status_ready)
                com.rescuedesk.ai.domain.model.ModelStatus.Incompatible ->
                    stringResource(R.string.settings_llm_status_incompatible)
                com.rescuedesk.ai.domain.model.ModelStatus.Error ->
                    stringResource(R.string.settings_llm_status_error)
            }
            Text(
                statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (llmStatus == com.rescuedesk.ai.domain.model.ModelStatus.Ready)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (llmStatus != com.rescuedesk.ai.domain.model.ModelStatus.Ready) {
                Text(
                    stringResource(R.string.settings_llm_install_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                llmEngine?.lastError()?.let { err ->
                    Text(
                        err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.settings_delete_dialog_title)) },
            text = { Text(stringResource(R.string.settings_delete_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            // Personal tables first, then the device records about
                            // the user (onboarding stamp, sync history). Language and
                            // text size are accessibility choices — deliberately kept.
                            ServiceLocator.familyRepository.deleteAllPersonalData()
                            store.resetPersonalization()
                            android.widget.Toast
                                .makeText(context, R.string.settings_delete_done, android.widget.Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                ) {
                    Text(
                        stringResource(R.string.settings_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.settings_delete_cancel))
                }
            }
        )
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
