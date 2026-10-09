package com.rescuedesk.ai.app

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rescuedesk.ai.ai.engine.AiEngine
import com.rescuedesk.ai.ai.engine.MediaPipeEngine
import com.rescuedesk.ai.data.local.PreferencesStore
import com.rescuedesk.ai.data.local.RescueDeskDatabase
import com.rescuedesk.ai.data.pack.PackRepository
import com.rescuedesk.ai.data.repository.FamilyRepository
import com.rescuedesk.ai.data.repository.GuideRepository
import com.rescuedesk.ai.data.repository.RoomFamilyRepository
import com.rescuedesk.ai.data.repository.RoomGuideRepository
import com.rescuedesk.ai.data.seed.BuiltInGuideSeeder
import com.rescuedesk.ai.work.PackSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Manual dependency graph — deliberate for a one-developer scaffold; adopt Hilt
 * only if the module count justifies it.
 */
object ServiceLocator {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var guideRepository: GuideRepository
        private set

    lateinit var familyRepository: FamilyRepository
        private set

    lateinit var packRepository: PackRepository
        private set

    lateinit var preferencesStore: PreferencesStore
        private set

    lateinit var aiEngine: AiEngine
        private set

    /**
     * Concrete on-device LLM adapter for the debug-flagged "Try local LLM"
     * path (Settings → Experimental). Null only if MediaPipe native libs
     * refuse to load on this device — AskViewModel handles the null case
     * and stays on the retrieval-grounded composition path.
     */
    var mediaPipeEngine: MediaPipeEngine? = null
        private set

    fun init(context: Context) {
        val db = RescueDeskDatabase.create(context)
        preferencesStore = PreferencesStore(context)
        guideRepository = RoomGuideRepository(
            guideDao = db.guideDao(),
            seeder = BuiltInGuideSeeder(context, db.guideDao()),
            preferences = preferencesStore
        )
        familyRepository = RoomFamilyRepository(
            planDao = db.familyPlanDao(),
            contactDao = db.contactDao(),
            goBagDao = db.goBagDao()
        )
        packRepository = PackRepository(
            guideDao = db.guideDao(),
            preferences = preferencesStore
        )
        // Phase 1 gate is still pending. MediaPipeEngine is wired but lazy —
        // it does not load the ~1.4 GB Gemma task file until ensureLoaded() is
        // explicitly called, which AskViewModel only does when the user turns
        // on Settings → Experimental → "Try local LLM". Off by default, the
        // shipping retrieval-grounded path is unaffected.
        val candidateEngine = MediaPipeEngine(context.applicationContext)
        mediaPipeEngine = candidateEngine
        aiEngine = candidateEngine

        // Warm the built-in guide seed (PRD §5.4) so first offline launch is ready.
        scope.launch { guideRepository.ensureSeeded() }
        scope.launch { familyRepository.ensureGoBagSeeded() }

        // Daily guide-pack check honoring the Wi-Fi-only preference (PRD §5.12).
        scope.launch {
            val wifiOnly = preferencesStore.settings.first().wifiOnlyDownloads
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PackSyncWorker.UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PackSyncWorker.periodicRequest(wifiOnly)
            )
        }
    }
}
