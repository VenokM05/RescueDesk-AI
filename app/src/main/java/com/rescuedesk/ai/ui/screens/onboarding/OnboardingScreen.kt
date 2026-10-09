package com.rescuedesk.ai.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rescuedesk.ai.R
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.data.local.AppLanguage
import com.rescuedesk.ai.data.local.TextSize
import com.rescuedesk.ai.ui.labelRes
import kotlinx.coroutines.launch

/**
 * Screens A–C — first-run onboarding (PRD section 5.2, section 5.3): welcome, language
 * choice, and text-size choice. Skippable; never blocks access to content.
 * Choices apply immediately via PreferencesStore.
 */
@Composable
fun OnboardingScreen(settings: AppSettingsSnapshot, onFinished: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var language by remember { mutableStateOf(settings.language) }
    var textSize by remember { mutableStateOf(settings.textSize) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.Top)
    ) {
        when (step) {
            0 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Image(
                    painter = painterResource(R.drawable.logo_horizontal),
                    contentDescription = "RescueDesk AI",
                    modifier = Modifier.height(56.dp).width(168.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.onboarding_welcome_title_fil),
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    stringResource(R.string.onboarding_welcome_title_en),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FeatureLine(stringResource(R.string.onboarding_feature_offline))
                FeatureLine(stringResource(R.string.onboarding_feature_account))
                FeatureLine(stringResource(R.string.onboarding_feature_guides))
            }
            1 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    stringResource(R.string.onboarding_language_title),
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    stringResource(R.string.onboarding_language_sub),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ChoiceCard(
                    title = AppLanguage.FILIPINO.label,
                    subtitle = stringResource(R.string.onboarding_language_fil_sub),
                    selected = language == AppLanguage.FILIPINO,
                    onClick = {
                        language = AppLanguage.FILIPINO
                        scope.launch { ServiceLocator.preferencesStore.setLanguage(AppLanguage.FILIPINO) }
                    }
                )
                ChoiceCard(
                    title = AppLanguage.ENGLISH.label,
                    subtitle = stringResource(R.string.onboarding_language_en_sub),
                    selected = language == AppLanguage.ENGLISH,
                    onClick = {
                        language = AppLanguage.ENGLISH
                        scope.launch { ServiceLocator.preferencesStore.setLanguage(AppLanguage.ENGLISH) }
                    }
                )
            }
            else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    stringResource(R.string.onboarding_textsize_title),
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    stringResource(R.string.onboarding_textsize_sub),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextSize.entries.forEach { size ->
                    ChoiceCard(
                        title = stringResource(size.labelRes()),
                        subtitle = stringResource(R.string.onboarding_textsize_sample),
                        subtitleScale = size.factor,
                        selected = textSize == size,
                        onClick = {
                            textSize = size
                            scope.launch { ServiceLocator.preferencesStore.setTextSize(size) }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (step < 2) step++ else {
                    scope.launch {
                        ServiceLocator.preferencesStore.setOnboardingCompleted(true)
                        onFinished()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Text(
                text = stringResource(
                    when (step) {
                        0 -> R.string.onboarding_get_started
                        1 -> R.string.common_next
                        else -> R.string.onboarding_start_app
                    }
                ),
                style = MaterialTheme.typography.labelLarge
            )
        }
        if (step > 0) {
            TextButton(onClick = { step-- }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(R.string.common_back))
            }
        }
        // Skippable: onboarding must never gate emergency content (PRD section 5.2).
        TextButton(
            onClick = {
                scope.launch {
                    ServiceLocator.preferencesStore.setOnboardingCompleted(true)
                    onFinished()
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.common_skip), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FeatureLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    subtitleScale: Float = 1.0f,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = shape
            )
            // Selection must not be color-only (PRD section 4.2). Announce the option,
            // the radio-group role, and the selected state to TalkBack.
            .semantics(mergeDescendants = true) {
                role = Role.RadioButton
                this.selected = selected
                contentDescription = title
            }
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * subtitleScale
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
            }
            if (selected) {
                // Decorative check — the semantics above already announce "selected".
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** Minimal snapshot so the onboarding composable stays free of flow plumbing. */
data class AppSettingsSnapshot(
    val language: AppLanguage?,
    val textSize: TextSize
)
