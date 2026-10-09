package com.rescuedesk.ai.data.repository

import com.rescuedesk.ai.data.local.GuideDao
import com.rescuedesk.ai.data.local.GuideEntity
import com.rescuedesk.ai.data.local.PreferencesStore
import com.rescuedesk.ai.data.search.FilipinoQueryExpander
import com.rescuedesk.ai.data.seed.BuiltInGuideSeeder
import com.rescuedesk.ai.domain.model.Guide
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface GuideRepository {
    fun observeGuides(): Flow<List<Guide>>
    fun searchGuides(query: String): Flow<List<Guide>>
    suspend fun guideById(id: Long): Guide?
    suspend fun ensureSeeded()
}

/**
 * Single source of truth for guide access (FR-01). UI and retrieval both read
 * through this repository, so built-in and pack content are treated uniformly.
 * Flows emit empty until the first-launch seed completes; collection resumes
 * automatically when it flips.
 */
class RoomGuideRepository(
    private val guideDao: GuideDao,
    private val seeder: BuiltInGuideSeeder,
    private val preferences: PreferencesStore
) : GuideRepository {

    private val seedMutex = Mutex()
    private val seeded = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeGuides(): Flow<List<Guide>> =
        seeded.flatMapLatest { ready ->
            if (ready) guideDao.observeAll() else flowOf(emptyList())
        }.localized().mapEntities()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun searchGuides(query: String): Flow<List<Guide>> =
        seeded.flatMapLatest { ready ->
            if (!ready) return@flatMapLatest flowOf(emptyList())
            val cleaned = query.trim().replace("\"", " ")
            if (cleaned.isBlank()) {
                guideDao.observeAll()
            } else {
                // Prefix matching per token, OR-joined for forgiving local typing
                // (PRD section 5.9: show results before a complete question is typed),
                // with conservative Filipino morphology expansion so conjugated
                // forms still reach guide roots (data/search/FilipinoQueryExpander).
                val terms = cleaned.split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                    .flatMap { FilipinoQueryExpander.candidates(it) }
                    .distinct()
                    .take(MAX_QUERY_TERMS)
                    .joinToString(" OR ") { "$it*" }
                if (terms.isBlank()) guideDao.observeAll() else guideDao.search(terms)
            }
        }.localized().mapEntities()

    override suspend fun guideById(id: Long): Guide? = guideDao.byId(id)?.toDomain()

    override suspend fun ensureSeeded() {
        seedMutex.withLock {
            if (!seeded.value) {
                seeder.seedIfNeeded()
                seeded.value = true
            }
        }
    }

    private companion object {
        // Guard against a pathological long query exploding the MATCH string.
        const val MAX_QUERY_TERMS = 24
    }

    private fun Flow<List<GuideEntity>>.mapEntities(): Flow<List<Guide>> = map { list -> list.map { it.toDomain() } }

    /**
     * Language preference (FR-07): show the preferred-language version of each
     * topic; fall back to whatever exists when the topic has no such version,
     * so content coverage gaps never hide a guide.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun Flow<List<GuideEntity>>.localized(): Flow<List<GuideEntity>> =
        preferences.languageTag.flatMapLatest { wanted -> map { selectByLanguage(it, wanted) } }

    private fun selectByLanguage(entities: List<GuideEntity>, wanted: String): List<GuideEntity> {
        if (entities.isEmpty() || wanted.isBlank()) return entities
        return entities.groupBy { topicKey(it.publicId) }
            .flatMap { (_, sameTopic) ->
                sameTopic.filter { it.language == wanted }.ifEmpty { sameTopic }
            }
            // Stable library order (category, title) after the regrouping.
            .sortedWith(compareBy({ it.category }, { it.title }))
    }

    /** Strips a trailing language segment so en/fil variants of one topic group. */
    private fun topicKey(publicId: String): String =
        publicId.replace(Regex("-(en|fil|tl|eng|filipino)$", RegexOption.IGNORE_CASE), "")

    private fun GuideEntity.toDomain() = Guide(
        id = id,
        publicId = publicId,
        title = title,
        category = category,
        summary = summary,
        body = body,
        avoid = avoid,
        language = language,
        sourceName = sourceName,
        sourceRef = sourceRef,
        lastReviewed = lastReviewed,
        nextReview = nextReview,
        version = version,
        rightsStatus = rightsStatus,
        isBuiltin = isBuiltin
    )
}
