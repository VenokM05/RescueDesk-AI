package com.rescuedesk.ai.ui.screens.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rescuedesk.ai.R
import com.rescuedesk.ai.domain.model.HouseholdPlan

private val STEP_TITLE_RES = listOf(
    R.string.wizard_step_1,
    R.string.wizard_step_2,
    R.string.wizard_step_3,
    R.string.wizard_step_4,
    R.string.wizard_step_5
)

/**
 * Family Plan Wizard (PRD §5.10): five short steps instead of one long form.
 * Each step saves on "Next" (FR-05: progress saves locally, survives restart).
 */
@Composable
fun PlanWizardScreen(
    startStep: Int,
    onBack: () -> Unit,
    onOpenGoBag: () -> Unit,
    viewModel: FamilyViewModel = viewModel()
) {
    val plan by viewModel.plan.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    var step by remember { mutableIntStateOf(startStep.coerceIn(0, 4)) }
    // Local draft seeded once from the first real DB emission (plan is null until then).
    var draft by remember { mutableStateOf<HouseholdPlan?>(null) }
    LaunchedEffect(plan) {
        if (draft == null && plan != null) draft = plan
    }

    val current = draft ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
        Text(
            stringResource(STEP_TITLE_RES[step]),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        when (step) {
            0 -> HouseholdStep(
                plan = current,
                onChange = { draft = it }
            )
            1 -> ContactsEditor(viewModel)
            2 -> MeetingStep(
                plan = current,
                onChange = { draft = it }
            )
            3 -> SuppliesStep(onOpenGoBag = onOpenGoBag)
            else -> ReviewStep(
                plan = current,
                contacts = contacts,
                onChange = { draft = it },
                onEditStep = { step = it }
            )
        }

        if (step < 4) {
            Button(
                onClick = {
                    // Per-step autosave (FR-05).
                    if (step != 1) viewModel.savePlan(current)
                    step++
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)
            ) {
                Text(stringResource(R.string.common_next), style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Button(
                onClick = {
                    viewModel.savePlan(current.copy(planCompleted = true))
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)
            ) {
                Text(stringResource(R.string.wizard_finish), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun HouseholdStep(plan: HouseholdPlan, onChange: (HouseholdPlan) -> Unit) {
    OutlinedTextField(
        value = plan.householdNickname,
        onValueChange = { onChange(plan.copy(householdNickname = it)) },
        label = { Text(stringResource(R.string.wizard_nickname_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = if (plan.memberCount == 0) "" else plan.memberCount.toString(),
        onValueChange = { raw ->
            onChange(plan.copy(memberCount = raw.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0))
        },
        label = { Text(stringResource(R.string.wizard_members_label)) },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = plan.householdNotes,
        onValueChange = { onChange(plan.copy(householdNotes = it)) },
        label = { Text(stringResource(R.string.wizard_needs_label)) },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MeetingStep(plan: HouseholdPlan, onChange: (HouseholdPlan) -> Unit) {
    OutlinedTextField(
        value = plan.meetingNearby,
        onValueChange = { onChange(plan.copy(meetingNearby = it)) },
        label = { Text(stringResource(R.string.wizard_meeting_nearby_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = plan.meetingAlternate,
        onValueChange = { onChange(plan.copy(meetingAlternate = it)) },
        label = { Text(stringResource(R.string.wizard_meeting_alt_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(4.dp))
    Text(
        stringResource(R.string.wizard_outofarea_note),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedTextField(
        value = plan.outOfAreaName,
        onValueChange = { onChange(plan.copy(outOfAreaName = it)) },
        label = { Text(stringResource(R.string.wizard_outofarea_name_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = plan.outOfAreaPhone,
        onValueChange = { onChange(plan.copy(outOfAreaPhone = it)) },
        label = { Text(stringResource(R.string.wizard_outofarea_phone_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SuppliesStep(onOpenGoBag: () -> Unit) {
    Text(
        stringResource(R.string.wizard_supplies_body),
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        stringResource(R.string.wizard_supplies_note),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedButton(
        onClick = onOpenGoBag,
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)
    ) {
        Text(stringResource(R.string.wizard_open_gobag), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ReviewStep(
    plan: HouseholdPlan,
    contacts: List<com.rescuedesk.ai.domain.model.EmergencyContact>,
    onChange: (HouseholdPlan) -> Unit,
    onEditStep: (Int) -> Unit
) {
    ReviewRow(stringResource(R.string.wizard_review_household), plan.householdNickname.ifBlank { "—" }, { onEditStep(0) })
    ReviewRow(
        stringResource(R.string.wizard_review_members),
        if (plan.memberCount > 0) plan.memberCount.toString() else "—",
        { onEditStep(0) }
    )
    ReviewRow(
        stringResource(R.string.wizard_review_contacts),
        if (contacts.isEmpty()) stringResource(R.string.common_none_saved)
        else contacts.joinToString(", ") { "${it.name} · ${it.phone}" },
        { onEditStep(1) }
    )
    ReviewRow(
        stringResource(R.string.wizard_review_meeting_nearby),
        plan.meetingNearby.ifBlank { "—" },
        { onEditStep(2) }
    )
    ReviewRow(
        stringResource(R.string.wizard_review_meeting_alt),
        plan.meetingAlternate.ifBlank { "—" },
        { onEditStep(2) }
    )
    ReviewRow(
        stringResource(R.string.wizard_review_outofarea),
        listOf(plan.outOfAreaName, plan.outOfAreaPhone).filter { it.isNotBlank() }
            .joinToString(" · ").ifBlank { "—" },
        { onEditStep(2) }
    )

    Text(
        stringResource(R.string.wizard_review_reminders),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    OutlinedTextField(
        value = plan.reminders,
        onValueChange = { onChange(plan.copy(reminders = it)) },
        label = { Text(stringResource(R.string.wizard_reminders_label)) },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ReviewRow(label: String, value: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
        TextButton(onClick = onEdit) { Text(stringResource(R.string.common_edit)) }
    }
}
