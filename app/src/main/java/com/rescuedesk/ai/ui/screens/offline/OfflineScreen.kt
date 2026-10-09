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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
                title = { Text("Offline & downloads") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
    val (icon, label) = when (status) {
        PackStatus.Idle -> Icons.Default.CloudOff to "Not checked yet on this device"
        PackStatus.Checking -> Icons.Default.CloudSync to "Checking for guide updates…"
        is PackStatus.Downloading ->
            Icons.Default.CloudSync to "Downloading guides ${status.done + 1} of ${status.total}…"
        PackStatus.Applying -> Icons.Default.CloudSync to "Applying downloaded guides…"
        is PackStatus.UpToDate -> Icons.Default.Done to
            if (status.downloaded > 0) "Update installed — ${status.downloaded} new, ${status.skipped} already current"
            else "Up to date — no new guides found"
        is PackStatus.Failed -> Icons.Default.ErrorOutline to "Could not update: ${status.reason}"
    }
    SectionCard(title = "Connection status") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        FilledTonalButton(
            onClick = onCheck,
            enabled = !syncing,
            modifier = Modifier.padding(top = 8.dp)
        ) { Text(if (syncing) "Working…" else "Check for updates") }
    }
}

@Composable
private fun GuidePackCard(info: PackRepository.PackInfo?, lastSync: String?) {
    SectionCard(title = "Emergency Guide Pack") {
        val installed = info != null && info.guideCount > 0
        InfoRow("Built-in guides", "4 categories always on this device, no download needed")
        InfoRow(
            "Downloaded guides",
            if (installed) "${info!!.guideCount} guides, pack version ${info.latestVersion}"
            else "None yet — built-in guides still work offline"
        )
        InfoRow(
            "Storage used by downloads",
            if (installed) "~${formatBytes(info!!.approxBytes)} (approx.)" else "0 KB"
        )
        InfoRow("Last update check", lastSync ?: "Never")
        Text(
            "Downloads are verified by checksum and reviewer-cleared rights before " +
                "they replace anything on this device. If a download fails, your " +
                "current guides are kept unchanged.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun WifiCard(wifiOnly: Boolean, onSetWifiOnly: (Boolean) -> Unit) {
    SectionCard(title = "Download preference") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.Checkbox(checked = wifiOnly, onCheckedChange = onSetWifiOnly)
            Text(
                "Download guides over Wi-Fi only",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            "Guides are small text files, but this protects prepaid data plans. " +
                "\"Check for updates\" above always runs when you press it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AiModelCard() {
    SectionCard(title = "AI model (Ask AI)") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Not installed — Ask AI shows offline guide answers only",
                style = MaterialTheme.typography.bodyLarge)
        }
        Text(
            "The on-device AI model needs several times more storage and memory " +
                "than the whole guide library, and its availability depends on the " +
                "Phase 1 on-device performance review. Emergency guides never " +
                "depend on the model: removing or skipping it keeps all guides intact.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
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
