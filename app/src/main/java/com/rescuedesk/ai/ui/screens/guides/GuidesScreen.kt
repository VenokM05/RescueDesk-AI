package com.rescuedesk.ai.ui.screens.guides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rescuedesk.ai.R
import com.rescuedesk.ai.domain.model.Guide

/**
 * Screen I — Guides Library (PRD section 5.9).
 * Works fully offline; usable even when the AI model is absent (acceptance criterion).
 */
@Composable
fun GuidesScreen(
    onOpenGuide: (Long) -> Unit,
    viewModel: GuidesViewModel = viewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val guides by viewModel.results.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.guides_title), style = MaterialTheme.typography.displaySmall)
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            label = { Text(stringResource(R.string.guides_search_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (guides.isEmpty()) {
            Text(
                text = stringResource(R.string.guides_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(guides, key = { it.id }) { guide -> GuideRow(guide, onClick = { onOpenGuide(guide.id) }) }
        }
    }
}

@Composable
private fun GuideRow(guide: Guide, onClick: () -> Unit) {
    // Screen G opens on tap; the row itself stays a summary only (PRD section 5.9).
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(guide.title, style = MaterialTheme.typography.titleLarge)
            Text(guide.summary, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Status is never color-only (PRD section 4.2): labels carry the meaning.
                Text(
                    text = stringResource(
                        if (guide.isBuiltin) R.string.common_builtin_badge
                        else R.string.common_pack_badge
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (guide.needsReview(java.time.LocalDate.now().toString())) {
                    Text(
                        text = stringResource(R.string.common_needs_review),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
