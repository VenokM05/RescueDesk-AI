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
     */
    @Query(
        """
        SELECT g.* FROM guides AS g
        JOIN guides_fts AS f ON g.id = f.rowid
        WHERE guides_fts MATCH :query
        ORDER BY f.rank
        """
    )
    fun search(query: String): Flow<List<GuideEntity>>

    @Query("DELETE FROM guides")
    suspend fun clearAll()
}
