package com.rescuedesk.ai.data.repository

import com.rescuedesk.ai.data.local.GuideDao
import com.rescuedesk.ai.data.local.GuideEntity
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
    private val seeder: BuiltInGuideSeeder
) : GuideRepository {

    private val seedMutex = Mutex()
    private val seeded = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeGuides(): Flow<List<Guide>> =
        seeded.flatMapLatest { ready ->
            if (ready) guideDao.observeAll() else flowOf(emptyList())
        }.mapEntities()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun searchGuides(query: String): Flow<List<Guide>> =
        seeded.flatMapLatest { ready ->
            if (!ready) return@flatMapLatest flowOf(emptyList())
            val cleaned = query.trim().replace("\"", " ")
            if (cleaned.isBlank()) {
                guideDao.observeAll()
            } else {
                // Prefix matching per token, OR-joined for forgiving local typing
                // (PRD §5.9: show results before a complete question is typed).
                val match = cleaned.split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                    .joinToString(" OR ") { "$it*" }
                guideDao.search(match)
            }
        }.mapEntities()

    override suspend fun guideById(id: Long): Guide? = guideDao.byId(id)?.toDomain()

    override suspend fun ensureSeeded() {
        seedMutex.withLock {
            if (!seeded.value) {
                seeder.seedIfNeeded()
                seeded.value = true
            }
        }
    }

    private fun Flow<List<GuideEntity>>.mapEntities(): Flow<List<Guide>> = map { list -> list.map { it.toDomain() } }

    private fun GuideEntity.toDomain() = Guide(
        id = id,
        publicId = publicId,
        title = title,
        category = category,
        summary = summary,
        body = body,
        language = language,
        sourceName = sourceName,
        lastReviewed = lastReviewed,
        nextReview = nextReview,
        version = version,
        rightsStatus = rightsStatus,
        isBuiltin = isBuiltin
    )
}
