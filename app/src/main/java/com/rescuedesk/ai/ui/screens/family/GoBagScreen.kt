package com.rescuedesk.ai.ui.screens.family

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rescuedesk.ai.R
import com.rescuedesk.ai.domain.model.GoBagCategories
import com.rescuedesk.ai.domain.model.GoBagItem
import com.rescuedesk.ai.ui.goBagCategoryLabelRes

/**
 * Screen K — Go-Bag Checklist (PRD §5.11). Grouped categories, large
 * checkboxes, immediate save, add/remove custom items, last-updated date,
 * and wording that avoids one-size-fits-all claims.
 */
@Composable
fun GoBagScreen(
    onBack: () -> Unit,
    viewModel: GoBagViewModel = viewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val languageTag by viewModel.languageTag.collectAsStateWithLifecycle()
    val packed = items.count { it.checked }
    val lastUpdated = items.map { it.updatedAt }.filter { it.isNotEmpty() }.maxOrNull()
    // Hoist for use inside the non-composable semantics lambda below.
    val progressDesc = stringResource(R.string.gobag_progress, packed, items.size)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
                Text(
                    stringResource(R.string.gobag_title),
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    stringResource(R.string.gobag_disclaimer),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.gobag_progress, packed, items.size) +
                        (lastUpdated?.let {
                            stringResource(R.string.gobag_last_updated, it)
                        } ?: ""),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                LinearProgressIndicator(
                    progress = { if (items.isEmpty()) 0f else packed.toFloat() / items.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        // Progress bar is decoration; text label above carries the meaning,
                        // but announce a live snapshot so TalkBack reads it when it changes.
                        .semantics { contentDescription = progressDesc }
                )
                Spacer(Modifier.height(6.dp))
                AddItemRow(onAdd = viewModel::add)
                Spacer(Modifier.height(6.dp))
            }
        }

        GoBagCategories.ALL.forEach { category ->
            val inCategory = items.filter { it.category == category }
            if (inCategory.isNotEmpty()) {
                item {
                    val labelRes = goBagCategoryLabelRes(category)
                    Text(
                        if (labelRes != null) stringResource(labelRes) else category,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                items(inCategory, key = { it.id }) { item ->
                    GoBagRow(
                        label = item.labelFor(languageTag),
                        item = item,
                        onToggle = { viewModel.toggle(item.id, it) },
                        onRemove = { viewModel.remove(item.id) }
                    )
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun GoBagRow(
    label: String,
    item: GoBagItem,
    onToggle: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.checked,
            onCheckedChange = onToggle,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .width(56.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (item.isCustom) {
            TextButton(onClick = onRemove) {
                Text(stringResource(R.string.common_remove), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddItemRow(onAdd: (String, String) -> Unit) {
    var label by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(GoBagCategories.ALL.last()) }

    Text(stringResource(R.string.gobag_add_section), style = MaterialTheme.typography.titleLarge)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        GoBagCategories.ALL.forEach { cat ->
            val labelRes = goBagCategoryLabelRes(cat)
            SuggestionChip(
                onClick = { category = cat },
                label = {
                    Text(
                        if (labelRes != null) stringResource(labelRes) else cat,
                        fontWeight = if (cat == category) FontWeight.Bold else FontWeight.Normal
                    )
                },
                // PRD §4.4: minimum 48dp tap target even at small text scale.
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text(stringResource(R.string.gobag_item_label)) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(10.dp))
        Button(
            onClick = {
                onAdd(category, label)
                label = ""
            },
            modifier = Modifier.heightIn(min = 56.dp)
        ) {
            Text(stringResource(R.string.common_add))
        }
    }
}
