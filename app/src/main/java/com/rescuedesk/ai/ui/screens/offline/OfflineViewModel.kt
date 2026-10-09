package com.rescuedesk.ai.ui.screens.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.data.local.AppSettings
import com.rescuedesk.ai.data.pack.PackRepository
import com.rescuedesk.ai.data.pack.PackStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Screen L — Offline & Download Manager (PRD §5.12). */
class OfflineViewModel : ViewModel() {

    private val pack = ServiceLocator.packRepository
    private val prefs = ServiceLocator.preferencesStore

    val status: StateFlow<PackStatus> = pack.status
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PackStatus.Idle)

    val packInfo: StateFlow<PackRepository.PackInfo?> = pack.packInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val settings: StateFlow<AppSettings?> = prefs.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val lastSync: StateFlow<String?> = prefs.settings
        .map { it.lastPackSync }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun checkForUpdates() {
        viewModelScope.launch { pack.sync() }
    }

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch { prefs.setWifiOnlyDownloads(enabled) }
    }
}
