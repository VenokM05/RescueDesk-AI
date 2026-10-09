package com.rescuedesk.ai.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rescuedesk.ai.R
import com.rescuedesk.ai.domain.model.Guide
import com.rescuedesk.ai.ui.theme.Amber

/**
 * Screen G — Guide Detail (PRD §5.7). Fixed reading order:
 * title → freshness/rights notices → "Do this first" → "Avoid these actions"
 * → sources and review dates → related guides. Large text, no hidden steps.
 */
@Composable
fun GuideDetailScreen(
    onBack: () -> Unit,
    onOpenGuide: (Long) -> Unit,
    viewModel: GuideDetailViewModel = viewModel()
) {
    val guide by viewModel.guide.collectAsStateWithLifecycle()
    val related by viewModel.related.collectAsStateWithLifecycle()

    val g = guide
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
        if (g == null) {
            Text(stringResource(R.string.detail_not_found), style = MaterialTheme.typography.bodyLarge)
            return@Column
        }
        GuideDetailContent(
            guide = g,
            related = related,
            onOpenGuide = onOpenGuide
        )
    }
}

@Composable
fun GuideDetailContent(guide: Guide, related: List<Guide>, onOpenGuide: (Long) -> Unit) {
    Text(guide.title, style = MaterialTheme.typography.displaySmall)
    Text(guide.summary, style = MaterialTheme.typography.bodyLarge)

    // Freshness warning (PRD §5.7 + §8.3): never color-only, always a text label.
    if (guide.needsReview(java.time.LocalDate.now().toString())) {
        val reviewedLabel = guide.lastReviewed
            ?: stringResource(R.string.detail_needs_review_never)
        WarningBanner(stringResource(R.string.detail_needs_review, reviewedLabel))
    }
    // Unreviewed/unrights-cleared placeholder disclosure (PRD §10.3).
    if (guide.rightsStatus != "cleared") {
        WarningBanner(stringResource(R.string.detail_rights_pending))
    }

    Text(
        stringResource(R.string.detail_do_this_first),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )
    guide.steps.forEach { step ->
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text(step, style = MaterialTheme.typography.bodyLarge)
        }
    }

    if (guide.avoidList.isNotEmpty()) {
        Text(
            stringResource(R.string.detail_avoid),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        guide.avoidList.forEach { item ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                // Decorative mark — the section header already carries the meaning
                // (PRD §4.2: status not color/glyph-only; text label is the source of truth).
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(10.dp))
                Text(item, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    Spacer(Modifier.height(6.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.detail_source), style = MaterialTheme.typography.titleLarge)
            Text(guide.sourceName, style = MaterialTheme.typography.bodyMedium)
            guide.sourceRef?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                stringResource(
                    if (guide.isBuiltin) R.string.detail_meta_builtin else R.string.detail_meta_pack,
                    guide.language.uppercase(),
                    guide.version
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (related.isNotEmpty()) {
        Text(stringResource(R.string.detail_related), style = MaterialTheme.typography.titleLarge)
        related.forEach { rel ->
            SuggestionChip(
                onClick = { onOpenGuide(rel.id) },
                label = { Text(rel.title) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WarningBanner(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .background(Amber.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    )
}
