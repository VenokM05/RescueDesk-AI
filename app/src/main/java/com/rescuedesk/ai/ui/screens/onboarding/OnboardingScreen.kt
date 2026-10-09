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
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rescuedesk.ai.R
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.data.local.AppLanguage
import com.rescuedesk.ai.data.local.TextSize
import kotlinx.coroutines.launch

/**
 * Screens A–C — first-run onboarding (PRD §5.2, §5.3): welcome, language
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
                    "Handa ka na ba sa emergency?",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    "Are you ready for an emergency?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FeatureLine("✓ Gumagana nang offline — works with no internet")
                FeatureLine("✓ Walang account — no sign-up, no accounts")
                FeatureLine("✓ Libreng gabay — free, reviewed emergency guides")
            }
            1 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Piliin ang wika", style = MaterialTheme.typography.displaySmall)
                Text("Choose your language", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                ChoiceCard(
                    title = "Filipino",
                    subtitle = "Gumamit ng Filipino sa app",
                    selected = language == AppLanguage.FILIPINO,
                    onClick = {
                        language = AppLanguage.FILIPINO
                        scope.launch { ServiceLocator.preferencesStore.setLanguage(AppLanguage.FILIPINO) }
                    }
                )
                ChoiceCard(
                    title = "English",
                    subtitle = "Use English in the app",
                    selected = language == AppLanguage.ENGLISH,
                    onClick = {
                        language = AppLanguage.ENGLISH
                        scope.launch { ServiceLocator.preferencesStore.setLanguage(AppLanguage.ENGLISH) }
                    }
                )
            }
            else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Laki ng teksto", style = MaterialTheme.typography.displaySmall)
                Text("Text size", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextSize.entries.forEach { size ->
                    ChoiceCard(
                        title = size.label,
                        subtitle = "Halimbawa: Lumikas papuntang matataas na lugar",
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
                text = when (step) {
                    0 -> "Magsimula (Get started)"
                    1 -> "Susunod (Next)"
                    else -> "Simulan na (Start using the app)"
                },
                style = MaterialTheme.typography.labelLarge
            )
        }
        if (step > 0) {
            TextButton(onClick = { step-- }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Bumalik (Back)")
            }
        }
        // Skippable: onboarding must never gate emergency content (PRD §5.2).
        TextButton(
            onClick = {
                scope.launch {
                    ServiceLocator.preferencesStore.setOnboardingCompleted(true)
                    onFinished()
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Laktawan (Skip)", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text(
                    "✓",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
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
