package com.rescuedesk.ai.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** App language choices (PRD section 5.2): Filipino or English; null = follow system. */
enum class AppLanguage(val tag: String, val label: String) {
    FILIPINO("fil", "Filipino"),
    ENGLISH("en", "English");

    companion object {
        fun fromTag(tag: String?): AppLanguage? = entries.firstOrNull { it.tag == tag }
    }
}

/** In-app text size multiplier choices (PRD section 5.3, complements system font scale). */
enum class TextSize(val factor: Float, val label: String) {
    NORMAL(1.0f, "Normal"),
    LARGE(1.15f, "Large"),
    EXTRA_LARGE(1.3f, "Extra large")
}

data class AppSettings(
    val language: AppLanguage? = null,
    val textSize: TextSize = TextSize.NORMAL,
    val onboardingCompleted: Boolean = false,
    /** PRD section 5.12: background guide downloads wait for unmetered networks. */
    val wifiOnlyDownloads: Boolean = true,
    val lastPackSync: String? = null,
    /**
     * Debug-only switch to route Ask Juan through the on-device Gemma 2 2B IT
     * adapter (MediaPipe). Off by default so the retrieval-grounded path stays
     * the shipping behaviour and the Phase 1 Go/No-Go gate is not pre-empted.
     */
    val llmEnabled: Boolean = false
)

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Small preferences layer: language, text size, onboarding state.
 * All values are device-local; nothing here is ever synced or logged (PRD section 2.2).
 */
class PreferencesStore(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            language = AppLanguage.fromTag(prefs[KEY_LANGUAGE]),
            textSize = prefs[KEY_TEXT_SIZE_FACTOR]?.let { f -> TextSize.entries.firstOrNull { it.factor == f } }
                ?: TextSize.NORMAL,
            onboardingCompleted = prefs[KEY_ONBOARDING] ?: false,
            wifiOnlyDownloads = prefs[KEY_WIFI_ONLY] ?: true,
            lastPackSync = prefs[KEY_LAST_SYNC]?.takeIf { it.isNotEmpty() },
            llmEnabled = prefs[KEY_LLM_ENABLED] ?: false
        )
    }

    suspend fun setLanguage(language: AppLanguage?) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language?.tag ?: "" }
    }

    /**
     * Resolved guide/content language tag ("fil" or "en"): the explicit choice,
     * or the system locale when the user chose "follow system" (FR-07).
     */
    val languageTag: Flow<String> = settings.map { choice ->
        choice.language?.tag ?: systemLanguageTag()
    }

    private fun systemLanguageTag(): String {
        val primary = context.resources.configuration.locales[0].language
        return if (primary == "fil" || primary == "tl") "fil" else "en"
    }

    suspend fun setTextSize(size: TextSize) {
        context.dataStore.edit { it[KEY_TEXT_SIZE_FACTOR] = size.factor }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING] = completed }
    }

    suspend fun setWifiOnlyDownloads(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WIFI_ONLY] = enabled }
    }

    suspend fun setLlmEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_LLM_ENABLED] = enabled }
    }

    suspend fun lastPackSync(): String? =
        context.dataStore.data.first()[KEY_LAST_SYNC]?.takeIf { it.isNotEmpty() }

    suspend fun setLastPackSync(dateIso: String) {
        context.dataStore.edit { it[KEY_LAST_SYNC] = dateIso }
    }

    /**
     * Companion of delete-all-personal-data (PRD section 5.13): the onboarding
     * consent/completion stamp and the sync history are device records about
     * the user, so they reset too. Language and text size are accessibility
     * choices, not personal data — they are deliberately KEPT so the user
     * immediately sees the app in their language afterwards.
     */
    suspend fun resetPersonalization() {
        context.dataStore.edit {
            it[KEY_ONBOARDING] = false
            it[KEY_LAST_SYNC] = ""
        }
    }

    private companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_TEXT_SIZE_FACTOR = floatPreferencesKey("text_size_factor")
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_completed")
        val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only_downloads")
        val KEY_LAST_SYNC = stringPreferencesKey("last_pack_sync")
        val KEY_LLM_ENABLED = booleanPreferencesKey("llm_enabled_experimental")
    }
}
