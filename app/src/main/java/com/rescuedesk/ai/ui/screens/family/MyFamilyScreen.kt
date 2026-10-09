package com.rescuedesk.ai.ui.screens.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Screen J — My Family (PRD §5.10). Overview of the five plan sections with
 * honest completion status; each opens the wizard step (or the go-bag list).
 * Everything shown here is stored on this device only.
 */
@Composable
fun MyFamilyScreen(
    onOpenWizardStep: (Int) -> Unit,
    onOpenGoBag: () -> Unit,
    viewModel: FamilyViewModel = viewModel(),
    goBagViewModel: GoBagViewModel = viewModel()
) {
    val plan by viewModel.plan.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val goBagItems by goBagViewModel.items.collectAsStateWithLifecycle()
    val loadedPlan = plan ?: return

    // Section completion, derived — never optimistic (PRD §5.10).
    val sectionsDone = listOf(
        loadedPlan.householdDone,
        contacts.isNotEmpty(),
        loadedPlan.meetingDone,
        goBagItems.any { it.checked },
        loadedPlan.remindersDone
    )
    val doneCount = sectionsDone.count { it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("My Family", style = MaterialTheme.typography.displaySmall)
        Text(
            text = if (loadedPlan.planCompleted) "Plan complete — keep it updated. Last saved ${loadedPlan.updatedAt}."
            else "$doneCount of 5 sections completed. Progress saves automatically on this device.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Everything you save here stays on this device unless you choose to share it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val sections = listOf(
            Triple("Family Emergency Plan", "Household, members, special needs", 0),
            Triple("Emergency Contacts", "Family and neighbors you would call", 1),
            Triple("Meeting Places", "Nearby, alternate, out-of-area contact", 2),
            Triple("Go-Bag Checklist", "Track what you have packed", -1),
            Triple("Important Reminders", "Notes your family should remember", 4)
        )
        sections.forEachIndexed { index, (label, subtitle, wizardStep) ->
            Button(
                onClick = { if (wizardStep >= 0) onOpenWizardStep(wizardStep) else onOpenGoBag() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.labelLarge)
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                    Text(
                        if (sectionsDone[index]) "✓ Completed" else "Not started",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
