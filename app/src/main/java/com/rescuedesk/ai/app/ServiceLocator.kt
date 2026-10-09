package com.rescuedesk.ai.app

import android.content.Context
import com.rescuedesk.ai.ai.engine.AiEngine
import com.rescuedesk.ai.ai.engine.UnavailableAiEngine
import com.rescuedesk.ai.data.local.PreferencesStore
import com.rescuedesk.ai.data.local.RescueDeskDatabase
import com.rescuedesk.ai.data.repository.GuideRepository
import com.rescuedesk.ai.data.repository.RoomGuideRepository
import com.rescuedesk.ai.data.seed.BuiltInGuideSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manual dependency graph — deliberate for a one-developer scaffold; adopt Hilt
 * only if the module count justifies it.
 */
object ServiceLocator {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var guideRepository: GuideRepository
        private set

    lateinit var preferencesStore: PreferencesStore
        private set

    lateinit var aiEngine: AiEngine
        private set

    fun init(context: Context) {
        val db = RescueDeskDatabase.create(context)
        preferencesStore = PreferencesStore(context)
        guideRepository = RoomGuideRepository(
            guideDao = db.guideDao(),
            seeder = BuiltInGuideSeeder(context, db.guideDao())
        )
        // Replace with the Phase 1 gate winner's runtime adapter (PRD §7.2).
        // Until then Ask AI renders the PRD §5.8 fallback state.
        aiEngine = UnavailableAiEngine()

        // Warm the built-in guide seed (PRD §5.4) so first offline launch is ready.
        scope.launch { guideRepository.ensureSeeded() }
    }
}
