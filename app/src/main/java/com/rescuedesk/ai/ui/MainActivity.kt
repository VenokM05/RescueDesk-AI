package com.rescuedesk.ai.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.ui.navigation.RescueDeskApp
import com.rescuedesk.ai.ui.screens.onboarding.AppSettingsSnapshot
import com.rescuedesk.ai.ui.screens.onboarding.OnboardingScreen
import com.rescuedesk.ai.ui.theme.RescueDeskTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val store = ServiceLocator.preferencesStore
            val settings by store.settings
                .collectAsStateWithLifecycle(initialValue = null)

            // Language override (PRD §5.2): wrap the Compose context with the
            // chosen locale so resources resolve accordingly; null = system.
            val appContext = LocalContext.current
            val langTag = settings?.language?.tag
            val localized = remember(appContext, langTag) {
                if (langTag == null) {
                    appContext
                } else {
                    val config = Configuration(appContext.resources.configuration)
                    config.setLocale(Locale.forLanguageTag(langTag))
                    appContext.createConfigurationContext(config)
                }
            }

            CompositionLocalProvider(
                LocalContext provides localized,
                LocalConfiguration provides localized.resources.configuration
            ) {
                RescueDeskTheme(textScaleFactor = settings?.textSize?.factor ?: 1.0f) {
                    // Unload any loaded model when the last screen goes away (PRD arch §5.3).
                    DisposableEffect(Unit) {
                        onDispose { ServiceLocator.aiEngine.unload() }
                    }
                    val current = settings
                    when {
                        // First frame before DataStore emits: show nothing rather
                        // than flash the wrong language/onboarding state.
                        current == null -> Unit
                        !current.onboardingCompleted -> OnboardingGate(current)
                        else -> RescueDeskApp()
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingGate(current: com.rescuedesk.ai.data.local.AppSettings) {
    OnboardingScreen(
        settings = AppSettingsSnapshot(language = current.language, textSize = current.textSize),
        // Flipping the persisted flag recomposes into the main app.
        onFinished = {}
    )
}
