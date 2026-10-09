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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rescuedesk.ai.domain.model.HouseholdPlan

private val STEP_TITLES = listOf(
    "Step 1: Household",
    "Step 2: Emergency Contacts",
    "Step 3: Meeting Places",
    "Step 4: Supplies",
    "Step 5: Review"
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
        TextButton(onClick = onBack) { Text("← Bumalik (Back)") }
        Text(
            STEP_TITLES[step],
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
            3 -> SuppliesStep(
                planCompletedNote = "You can add supplies any time — the checklist saves immediately.",
                onOpenGoBag = onOpenGoBag
            )
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
                Text("Susunod (Next)", style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Button(
                onClick = {
                    viewModel.savePlan(current.copy(planCompleted = true))
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)
            ) {
                Text("Mark plan complete", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun HouseholdStep(plan: HouseholdPlan, onChange: (HouseholdPlan) -> Unit) {
    OutlinedTextField(
        value = plan.householdNickname,
        onValueChange = { onChange(plan.copy(householdNickname = it)) },
        label = { Text("Household nickname (optional) — e.g. The Santos Family") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = if (plan.memberCount == 0) "" else plan.memberCount.toString(),
        onValueChange = { raw ->
            onChange(plan.copy(memberCount = raw.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0))
        },
        label = { Text("Number of household members") },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = plan.householdNotes,
        onValueChange = { onChange(plan.copy(householdNotes = it)) },
        label = { Text("Special needs (optional) — medicines, mobility aids, infants, pets") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MeetingStep(plan: HouseholdPlan, onChange: (HouseholdPlan) -> Unit) {
    OutlinedTextField(
        value = plan.meetingNearby,
        onValueChange = { onChange(plan.copy(meetingNearby = it)) },
        label = { Text("Meeting place nearby — e.g. gate of the barangay hall") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = plan.meetingAlternate,
        onValueChange = { onChange(plan.copy(meetingAlternate = it)) },
        label = { Text("Alternative meeting place — e.g. relative's house uphill") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "An out-of-area contact helps the family reconnect when local lines are down.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedTextField(
        value = plan.outOfAreaName,
        onValueChange = { onChange(plan.copy(outOfAreaName = it)) },
        label = { Text("Out-of-area contact name (optional)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = plan.outOfAreaPhone,
        onValueChange = { onChange(plan.copy(outOfAreaPhone = it)) },
        label = { Text("Out-of-area contact phone (optional)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SuppliesStep(planCompletedNote: String, onOpenGoBag: () -> Unit) {
    Text(
        "Track your go-bag supplies. Start with water, food, medicines, flashlight, and copies of important documents.",
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        planCompletedNote,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedButton(
        onClick = onOpenGoBag,
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)
    ) {
        Text("Open Go-Bag Checklist", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ReviewStep(
    plan: HouseholdPlan,
    contacts: List<com.rescuedesk.ai.domain.model.EmergencyContact>,
    onChange: (HouseholdPlan) -> Unit,
    onEditStep: (Int) -> Unit
) {
    ReviewRow("Household", plan.householdNickname.ifBlank { "—" }, { onEditStep(0) })
    ReviewRow("Members", if (plan.memberCount > 0) plan.memberCount.toString() else "—", { onEditStep(0) })
    ReviewRow(
        "Emergency contacts",
        if (contacts.isEmpty()) "None saved" else contacts.joinToString(", ") { "${it.name} · ${it.phone}" },
        { onEditStep(1) }
    )
    ReviewRow("Meeting place (nearby)", plan.meetingNearby.ifBlank { "—" }, { onEditStep(2) })
    ReviewRow("Meeting place (alternate)", plan.meetingAlternate.ifBlank { "—" }, { onEditStep(2) })
    ReviewRow(
        "Out-of-area contact",
        listOf(plan.outOfAreaName, plan.outOfAreaPhone).filter { it.isNotBlank() }
            .joinToString(" · ").ifBlank { "—" },
        { onEditStep(2) }
    )

    Text(
        "Important Reminders",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    OutlinedTextField(
        value = plan.reminders,
        onValueChange = { onChange(plan.copy(reminders = it)) },
        label = { Text("e.g. Lolo's medicine lasts 5 days; reunite at barangay hall first") },
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
        TextButton(onClick = onEdit) { Text("Edit") }
    }
}
