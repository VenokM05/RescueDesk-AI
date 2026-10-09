package com.rescuedesk.ai.ui.screens.guides

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.domain.model.Guide
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class GuidesViewModel(savedState: SavedStateHandle) : ViewModel() {

    private val repository = ServiceLocator.guideRepository
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** Optional category filter from the "guides?category={category}" route. */
    private val category: String? = savedState.get<String>("category")?.takeIf { it.isNotBlank() }

    /** Local, offline search over built-in + pack guides (FR-01, PRD §5.9). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<Guide>> =
        _query.flatMapLatest { q -> repository.searchGuides(q) }
            .map { list -> if (category == null) list else list.filter { it.category == category } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(value: String) {
        _query.value = value
    }
}
