package com.rescuedesk.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GuideDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(guide: GuideEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(guide: GuideEntity): Long

    @Query("SELECT COUNT(*) FROM guides")
    suspend fun count(): Int

    @Query("SELECT * FROM guides ORDER BY category, title")
    fun observeAll(): Flow<List<GuideEntity>>

    @Query("SELECT * FROM guides WHERE category = :category ORDER BY title")
    fun observeByCategory(category: String): Flow<List<GuideEntity>>

    @Query("SELECT * FROM guides WHERE id = :id")
    suspend fun byId(id: Long): GuideEntity?

    /**
     * Keyword search (PRD §7.3 "start with local keyword retrieval").
     * Callers must sanitize the query (strip quotes) before passing it in.
     * NOTE: Room's FTS4 validator does not expose the `rank` pseudo-column, so
     * ordering falls back to rowid. Relevance ranking (bm25 via FTS5 custom
     * SQLite build) is a Phase 1 PoC item — see docs/ARCHITECTURE.md §9.2.
     */
    @Query(
        """
        SELECT g.* FROM guides AS g
        JOIN guides_fts ON guides_fts.rowid = g.id
        WHERE guides_fts MATCH :query
        ORDER BY guides_fts.rowid
        """
    )
    fun search(query: String): Flow<List<GuideEntity>>

    @Query("DELETE FROM guides")
    suspend fun clearAll()
}
