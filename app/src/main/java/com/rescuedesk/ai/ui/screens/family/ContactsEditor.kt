package com.rescuedesk.ai.ui.screens.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rescuedesk.ai.R
import com.rescuedesk.ai.domain.model.EmergencyContact

/**
 * Shared contacts editor (FR-06, PRD §5.10 "Emergency Contacts Behavior"):
 * deliberate call action via the system dialer, number always visible, and
 * explicit wording that nobody is notified automatically.
 */
@Composable
fun ContactsEditor(viewModel: FamilyViewModel) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (contacts.isEmpty()) {
            Text(
                stringResource(R.string.contacts_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        contacts.forEach { contact ->
            ContactRow(
                contact = contact,
                onCall = { viewModel.dial(context, contact) },
                onDelete = { viewModel.deleteContact(contact.id) }
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.contacts_name_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = relationship,
            onValueChange = { relationship = it },
            label = { Text(stringResource(R.string.contacts_relationship_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(R.string.contacts_phone_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    viewModel.addContact(name, relationship, phone)
                    name = ""; relationship = ""; phone = ""
                }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) {
            Text(stringResource(R.string.contacts_add), style = MaterialTheme.typography.labelLarge)
        }
        Text(
            stringResource(R.string.contacts_dialing_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContactRow(contact: EmergencyContact, onCall: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    contact.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (contact.relationship.isNotBlank()) {
                    Text(contact.relationship, style = MaterialTheme.typography.bodyMedium)
                }
                // Number shown before dialing (PRD §5.10).
                Text(
                    contact.phone,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            TextButton(
                onClick = onCall,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.contacts_call), style = MaterialTheme.typography.labelLarge)
            }
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.contacts_delete), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
