package com.rescuedesk.ai.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.domain.model.Guide
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Loads one guide by database id for Screen G; also exposes related guides. */
class GuideDetailViewModel(savedState: SavedStateHandle) : ViewModel() {

    private val guideId: Long = savedState.get<Long>("guideId") ?: -1L

    val guide: StateFlow<Guide?> =
        ServiceLocator.guideRepository.observeGuides()
            .map { list -> list.firstOrNull { it.id == guideId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Same-category guides offered as "Related" at the bottom (PRD section 5.7). */
    val related: StateFlow<List<Guide>> =
        ServiceLocator.guideRepository.observeGuides()
            .map { list ->
                val current = list.firstOrNull { it.id == guideId }
                if (current == null) emptyList()
                else list.filter { it.category == current.category && it.id != current.id }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
