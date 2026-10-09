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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
        TextButton(onClick = onBack) { Text("← Bumalik (Back)") }
        if (g == null) {
            Text("Guide not found.", style = MaterialTheme.typography.bodyLarge)
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
        WarningBanner(
            "⚠ Needs review — this content may be outdated. " +
                "Last reviewed: ${guide.lastReviewed ?: "never"}."
        )
    }
    // Unreviewed/unrights-cleared placeholder disclosure (PRD §10.3).
    if (guide.rightsStatus != "cleared") {
        WarningBanner(
            "ℹ Pending review — this is preliminary content not yet approved by " +
                "an authorized reviewer or agency."
        )
    }

    Text(
        "Do this first",
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
            "Avoid these actions",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        guide.avoidList.forEach { item ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text("✗", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text(item, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    Spacer(Modifier.height(6.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nag-mula sa / Source", style = MaterialTheme.typography.titleLarge)
            Text(guide.sourceName, style = MaterialTheme.typography.bodyMedium)
            guide.sourceRef?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "Language: ${guide.language.uppercase()} · Version ${guide.version}" +
                    (if (guide.isBuiltin) " · Built-in guide" else " · Guide pack"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (related.isNotEmpty()) {
        Text("Related guides", style = MaterialTheme.typography.titleLarge)
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
