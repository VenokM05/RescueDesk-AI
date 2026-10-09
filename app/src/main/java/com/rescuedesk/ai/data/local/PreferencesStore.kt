package com.rescuedesk.ai.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** App language choices (PRD §5.2): Filipino or English; null = follow system. */
enum class AppLanguage(val tag: String, val label: String) {
    FILIPINO("fil", "Filipino"),
    ENGLISH("en", "English");

    companion object {
        fun fromTag(tag: String?): AppLanguage? = entries.firstOrNull { it.tag == tag }
    }
}

/** In-app text size multiplier choices (PRD §5.3, complements system font scale). */
enum class TextSize(val factor: Float, val label: String) {
    NORMAL(1.0f, "Normal"),
    LARGE(1.15f, "Large"),
    EXTRA_LARGE(1.3f, "Extra large")
}

data class AppSettings(
    val language: AppLanguage? = null,
    val textSize: TextSize = TextSize.NORMAL,
    val onboardingCompleted: Boolean = false
)

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Small preferences layer: language, text size, onboarding state.
 * All values are device-local; nothing here is ever synced or logged (PRD §2.2).
 */
class PreferencesStore(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            language = AppLanguage.fromTag(prefs[KEY_LANGUAGE]),
            textSize = prefs[KEY_TEXT_SIZE_FACTOR]?.let { f -> TextSize.entries.firstOrNull { it.factor == f } }
                ?: TextSize.NORMAL,
            onboardingCompleted = prefs[KEY_ONBOARDING] ?: false
        )
    }

    suspend fun setLanguage(language: AppLanguage?) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language?.tag ?: "" }
    }

    suspend fun setTextSize(size: TextSize) {
        context.dataStore.edit { it[KEY_TEXT_SIZE_FACTOR] = size.factor }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING] = completed }
    }

    private companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_TEXT_SIZE_FACTOR = floatPreferencesKey("text_size_factor")
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_completed")
    }
}
