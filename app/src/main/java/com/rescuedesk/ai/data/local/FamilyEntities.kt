package com.rescuedesk.ai.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Household plan (FR-05). Single-row table: one plan per device, stored
 * locally only — never synced, never uploaded (PRD section 5.10 privacy).
 */
@Entity(tableName = "family_plan")
data class FamilyPlanEntity(
    @PrimaryKey val id: Int = SINGLETON,
    val householdNickname: String = "",
    val memberCount: Int = 0,
    val householdNotes: String = "",
    val meetingNearby: String = "",
    val meetingAlternate: String = "",
    val outOfAreaName: String = "",
    val outOfAreaPhone: String = "",
    val reminders: String = "",
    val planCompleted: Boolean = false,
    val updatedAt: String = ""
) {
    companion object {
        const val SINGLETON = 1
    }
}

/** Saved emergency contact (FR-06). Phone is dialed via ACTION_DIAL only. */
@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val relationship: String,
    val phone: String
)

/**
 * One go-bag checklist item (PRD section 5.11). Defaults are seeded in both app
 * languages so the list follows the language setting without re-seeding;
 * custom items carry the same user text in both label fields.
 * [category] is a stable key resolved to a localized header by the UI.
 */
@Entity(tableName = "go_bag_items")
data class GoBagItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val label: String,
    val labelFil: String = "",
    val checked: Boolean = false,
    val isCustom: Boolean = false,
    val updatedAt: String = ""
)

@Dao
interface FamilyPlanDao {
    @Query("SELECT * FROM family_plan WHERE id = 1")
    fun observePlan(): Flow<FamilyPlanEntity?>

    @Upsert
    suspend fun upsert(entity: FamilyPlanEntity)

    @Query("DELETE FROM family_plan")
    suspend fun clear()

    /**
     * FR-07 / PRD section 5.13 "Delete all locally saved personal information":
     * every personal table in one transaction so deletion is all-or-nothing
     * (Room shares one DB connection per in-memory database, so the nested
     * DAO calls run inside this transaction).
     */
    @Transaction
    suspend fun deleteAllPersonalData(contactDao: ContactDao, goBagDao: GoBagDao) {
        clear()
        contactDao.clear()
        goBagDao.clear()
    }
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY id")
    fun observeAll(): Flow<List<EmergencyContactEntity>>

    @Insert
    suspend fun insert(entity: EmergencyContactEntity): Long

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM emergency_contacts")
    suspend fun clear()
}

@Dao
interface GoBagDao {
    @Query("SELECT * FROM go_bag_items ORDER BY category, id")
    fun observeAll(): Flow<List<GoBagItemEntity>>

    @Query("SELECT COUNT(*) FROM go_bag_items")
    suspend fun count(): Int

    @Insert
    suspend fun insert(entity: GoBagItemEntity)

    @Query("UPDATE go_bag_items SET checked = :checked, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean, updatedAt: String)

    @Query("DELETE FROM go_bag_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM go_bag_items")
    suspend fun clear()
}
