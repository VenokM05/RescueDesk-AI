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
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.rescuedesk.ai.ai.engine.MediaPipeEngine
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.data.local.AppLanguage
import com.rescuedesk.ai.data.local.TextSize
import com.rescuedesk.ai.ui.labelRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    // Experimental LLM: probe for a locally placed model file once per visit, and
    // expose a Re-check action so the user can register a file they just moved in
    // with the Files app WITHOUT restarting the app. Detection reads the filesystem
    // only (never loads the 2.7 GB model), so it stays on the IO dispatcher.
    // Every tap reports its outcome three ways: an instant Toast, a persistent
    // status line, and a logcat trace (tag LLM-detect) — a silent no-op tap is
    // never acceptable UX on a life-safety app, and the trace makes a "nothing
    // happens" report diagnosable over adb without a screen-reader session.
    val llmEngine = ServiceLocator.mediaPipeEngine
    var llmDetected by remember { mutableStateOf<MediaPipeEngine.DetectedModel?>(null) }
    var recheckedOnce by remember { mutableStateOf(false) }
    val recheckModel: () -> Unit = {
        scope.launch {
            android.widget.Toast.makeText(context, R.string.settings_llm_recheck_started, android.widget.Toast.LENGTH_SHORT).show()
            val result = withContext(Dispatchers.IO) {
                android.util.Log.i("LLM-detect", "engine=${llmEngine != null} probing candidates")
                llmEngine?.detectModel().also { d ->
                    android.util.Log.i(
                        "LLM-detect",
                        "detectModel -> " + (d?.let { "${it.path} (${it.bytes} B, valid=${it.valid})" } ?: "null")
                    )
                }
            }
            llmDetected = result
            recheckedOnce = true
            val sizeLabel = result?.let {
                android.text.format.Formatter.formatShortFileSize(context, it.bytes)
            }.orEmpty()
            val msg = when {
                result == null -> context.getString(R.string.settings_llm_recheck_none)
                result.valid -> context.getString(R.string.settings_llm_detected, result.path, sizeLabel)
                else -> context.getString(R.string.settings_llm_truncated, sizeLabel)
            }
            // MIUI/HyperOS can block app Notifications outright — a suppressed
            // Toast must never take the persistent status line down with it.
            runCatching {
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    LaunchedEffect(Unit) { recheckModel() }

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
        val llmStatus by (llmEngine?.status ?: kotlinx.coroutines.flow.MutableStateFlow(
            com.rescuedesk.ai.domain.model.ModelStatus.NotInstalled
        )).collectAsStateWithLifecycle(initialValue = com.rescuedesk.ai.domain.model.ModelStatus.NotInstalled)
        SectionCard(stringResource(R.string.settings_llm_section)) {
            Text(
                stringResource(R.string.settings_llm_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // The switch only unlocks when a complete, readable model file is
            // actually present (detectModel returned valid = true). Until then it
            // stays disabled and the locked-reason line + install hint below
            // explain why, so a disabled control never looks broken.
            val modelDetected = llmDetected?.valid == true
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
                    enabled = modelDetected,
                    onCheckedChange = { enabled ->
                        scope.launch { store.setLlmEnabled(enabled) }
                        // Try to load immediately when turning on so the
                        // status line reflects reality by the next frame.
                        if (enabled) {
                            scope.launch {
                                llmEngine?.ensureLoaded()
                                recheckModel()
                            }
                        } else llmEngine?.unload()
                    },
                    // Explicit colors so the control is unmistakable in BOTH
                    // light and dark themes: primary-filled track when checked,
                    // elevated surface track with an outline ring when off,
                    // and a dimmed variant of the same when disabled.
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                        disabledUncheckedTrackColor =
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                        disabledUncheckedThumbColor = MaterialTheme.colorScheme.outlineVariant,
                        disabledUncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
            if (!modelDetected) {
                // Reason line for the locked switch, shown right above the
                // existing download guidance so the two read as one block.
                Text(
                    stringResource(R.string.settings_llm_switch_locked),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            // Runtime presence: path + size when a complete file is found, or a
            // truncation warning when only a partial file is present. An explicit
            // "not found" line is shown after any Re-check that came up empty, so
            // every tap has a durable on-screen result, not just a success case.
            llmDetected?.let { d ->
                val sizeLabel = android.text.format.Formatter.formatShortFileSize(context, d.bytes)
                Text(
                    text = if (d.valid) {
                        stringResource(R.string.settings_llm_detected, d.path, sizeLabel)
                    } else {
                        stringResource(R.string.settings_llm_truncated, sizeLabel)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (d.valid) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
            if (recheckedOnce && llmDetected == null) {
                Text(
                    stringResource(R.string.settings_llm_recheck_none),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            // Register a file the user just moved in, without restarting the app.
            TextButton(onClick = { recheckModel() }) {
                Text(stringResource(R.string.settings_llm_recheck))
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
