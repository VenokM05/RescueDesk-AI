package com.rescuedesk.ai.ui.screens.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rescuedesk.ai.R
import com.rescuedesk.ai.data.pack.PackRepository
import com.rescuedesk.ai.data.pack.PackStatus

/**
 * Screen L — Offline and Download Manager (PRD §5.12).
 *
 * Connection status is never color-only: every state pairs an icon with a
 * text label. The AI-model section stays an honest placeholder until the
 * Phase 1 gate picks a runtime — no fake install button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineScreen(onBack: () -> Unit, viewModel: OfflineViewModel = viewModel()) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val packInfo by viewModel.packInfo.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val lastSync by viewModel.lastSync.collectAsStateWithLifecycle()

    val syncing = status is PackStatus.Checking ||
        status is PackStatus.Downloading ||
        status is PackStatus.Applying

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.offline_title)) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatusCard(status = status, syncing = syncing, onCheck = viewModel::checkForUpdates)

            GuidePackCard(info = packInfo, lastSync = lastSync)

            WifiCard(
                wifiOnly = settings?.wifiOnlyDownloads ?: true,
                onSetWifiOnly = viewModel::setWifiOnly
            )

            AiModelCard()
        }
    }
}

@Composable
private fun StatusCard(status: PackStatus, syncing: Boolean, onCheck: () -> Unit) {
    val statusLabel = when (status) {
        PackStatus.Idle -> stringResource(R.string.offline_status_idle)
        PackStatus.Checking -> stringResource(R.string.offline_status_checking)
        is PackStatus.Downloading ->
            stringResource(R.string.offline_status_downloading, status.done + 1, status.total)
        PackStatus.Applying -> stringResource(R.string.offline_status_applying)
        is PackStatus.UpToDate ->
            if (status.downloaded > 0) {
                stringResource(R.string.offline_status_updated, status.downloaded, status.skipped)
            } else {
                stringResource(R.string.offline_status_uptodate)
            }
        is PackStatus.Failed -> stringResource(R.string.offline_status_failed, status.reason)
    }
    val icon = when (status) {
        is PackStatus.Failed -> Icons.Default.ErrorOutline
        is PackStatus.UpToDate -> Icons.Default.Done
        PackStatus.Idle -> Icons.Default.CloudOff
        else -> Icons.Default.CloudSync
    }
    SectionCard(title = stringResource(R.string.offline_status_section)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(statusLabel, style = MaterialTheme.typography.bodyLarge)
        }
        FilledTonalButton(
            onClick = onCheck,
            enabled = !syncing,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                stringResource(if (syncing) R.string.offline_working else R.string.offline_check)
            )
        }
    }
}

@Composable
private fun GuidePackCard(info: PackRepository.PackInfo?, lastSync: String?) {
    SectionCard(title = stringResource(R.string.offline_pack_section)) {
        val installed = info != null && info.guideCount > 0
        InfoRow(
            stringResource(R.string.offline_pack_builtin),
            stringResource(R.string.offline_pack_builtin_value)
        )
        InfoRow(
            stringResource(R.string.offline_pack_downloaded),
            if (installed) stringResource(R.string.offline_pack_downloaded_value, info!!.guideCount, info.latestVersion)
            else stringResource(R.string.offline_pack_empty_value)
        )
        InfoRow(
            stringResource(R.string.offline_pack_storage),
            if (installed) stringResource(R.string.offline_pack_storage_value, formatBytes(info!!.approxBytes))
            else stringResource(R.string.offline_pack_storage_value, "0 B")
        )
        InfoRow(
            stringResource(R.string.offline_pack_last),
            lastSync ?: stringResource(R.string.common_never)
        )
        Text(
            stringResource(R.string.offline_pack_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun WifiCard(wifiOnly: Boolean, onSetWifiOnly: (Boolean) -> Unit) {
    SectionCard(title = stringResource(R.string.offline_pref_section)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.Checkbox(checked = wifiOnly, onCheckedChange = onSetWifiOnly)
            Text(
                stringResource(R.string.offline_pref_wifi),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            stringResource(R.string.offline_pref_wifi_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AiModelCard() {
    val engine = com.rescuedesk.ai.app.ServiceLocator.mediaPipeEngine
    val status by (engine?.status ?: kotlinx.coroutines.flow.MutableStateFlow(
        com.rescuedesk.ai.domain.model.ModelStatus.NotInstalled
    )).collectAsStateWithLifecycle(initialValue = com.rescuedesk.ai.domain.model.ModelStatus.NotInstalled)
    val icon = when (status) {
        com.rescuedesk.ai.domain.model.ModelStatus.Ready -> Icons.Default.Done
        com.rescuedesk.ai.domain.model.ModelStatus.Error,
        com.rescuedesk.ai.domain.model.ModelStatus.Incompatible -> Icons.Default.ErrorOutline
        com.rescuedesk.ai.domain.model.ModelStatus.Installing -> Icons.Default.CloudSync
        else -> Icons.Default.CloudOff
    }
    val label = when (status) {
        com.rescuedesk.ai.domain.model.ModelStatus.Ready ->
            stringResource(R.string.offline_model_ready)
        com.rescuedesk.ai.domain.model.ModelStatus.Installing ->
            stringResource(R.string.offline_model_loading)
        com.rescuedesk.ai.domain.model.ModelStatus.Incompatible ->
            stringResource(R.string.offline_model_incompatible)
        com.rescuedesk.ai.domain.model.ModelStatus.Error ->
            stringResource(R.string.offline_model_error)
        com.rescuedesk.ai.domain.model.ModelStatus.NotInstalled ->
            stringResource(R.string.offline_model_not_installed)
    }
    SectionCard(title = stringResource(R.string.offline_model_section)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        Text(
            stringResource(R.string.offline_model_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
        engine?.modelPathOrNull()?.let { path ->
            Text(
                stringResource(R.string.offline_model_path, path),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        // Update / Remove buttons appear only once a model is installable
        // (PRD §5.12); the guide library is deliberately unaffected by them.
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024 -> "%d KB".format(bytes / 1_024)
    else -> "$bytes B"
}
