package com.rescuedesk.ai.ui.screens.family

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuedesk.ai.R
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.domain.model.EmergencyContact
import com.rescuedesk.ai.domain.model.HouseholdPlan
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyViewModel : ViewModel() {

    private val repository = ServiceLocator.familyRepository

    // Null until the first DB emission so screens never seed drafts from the placeholder.
    val plan: StateFlow<HouseholdPlan?> = repository.observePlan()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val contacts: StateFlow<List<EmergencyContact>> = repository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun savePlan(updated: HouseholdPlan) {
        viewModelScope.launch { repository.savePlan(updated) }
    }

    fun addContact(name: String, relationship: String, phone: String) {
        viewModelScope.launch { repository.addContact(name, relationship, phone) }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch { repository.deleteContact(id) }
    }

    /**
     * FR-06 + PRD §10.2: ACTION_DIAL opens the system dialer pre-filled; the
     * user still presses call there. No CALL_PHONE permission, and we never
     * claim the contact was reached.
     */
    fun dial(context: Context, contact: EmergencyContact) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(contact.phone)}"))
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // LocalContext is the language-wrapped context, so this follows the setting.
            Toast.makeText(context, context.getString(R.string.contacts_no_dialer), Toast.LENGTH_LONG).show()
        }
    }
}
