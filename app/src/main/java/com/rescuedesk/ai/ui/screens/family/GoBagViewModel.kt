package com.rescuedesk.ai.ui.screens.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.domain.model.GoBagItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoBagViewModel : ViewModel() {

    private val repository = ServiceLocator.familyRepository

    val items: StateFlow<List<GoBagItem>> = repository.observeGoBag()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggle(id: Long, checked: Boolean) {
        viewModelScope.launch { repository.setGoBagChecked(id, checked) }
    }

    fun add(category: String, label: String) {
        if (label.isBlank()) return
        viewModelScope.launch { repository.addGoBagItem(category, label) }
    }

    fun remove(id: Long) {
        viewModelScope.launch { repository.deleteGoBagItem(id) }
    }
}
