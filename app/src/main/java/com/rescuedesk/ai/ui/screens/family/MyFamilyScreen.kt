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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rescuedesk.ai.R

/**
 * Screen J — My Family (PRD section 5.10). Overview of the five plan sections with
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

    // Section completion, derived — never optimistic (PRD section 5.10).
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
        Text(stringResource(R.string.family_title), style = MaterialTheme.typography.displaySmall)
        Text(
            text = if (loadedPlan.planCompleted) {
                stringResource(R.string.family_completed, loadedPlan.updatedAt)
            } else {
                stringResource(R.string.family_progress, doneCount)
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.family_private_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val sections = listOf(
            SectionCopy(R.string.family_section_plan, R.string.family_section_plan_sub, 0),
            SectionCopy(R.string.family_section_contacts, R.string.family_section_contacts_sub, 1),
            SectionCopy(R.string.family_section_meeting, R.string.family_section_meeting_sub, 2),
            SectionCopy(R.string.family_section_gobag, R.string.family_section_gobag_sub, -1),
            SectionCopy(R.string.family_section_reminders, R.string.family_section_reminders_sub, 4)
        )
        sections.forEachIndexed { index, section ->
            Button(
                onClick = { if (section.wizardStep >= 0) onOpenWizardStep(section.wizardStep) else onOpenGoBag() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(section.titleRes),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            stringResource(section.subtitleRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                    Text(
                        stringResource(
                            if (sectionsDone[index]) R.string.common_completed
                            else R.string.common_not_started
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/** One Screen J row: localized title/subtitle resources plus its wizard step (-1 = go-bag). */
private data class SectionCopy(
    val titleRes: Int,
    val subtitleRes: Int,
    val wizardStep: Int
)
