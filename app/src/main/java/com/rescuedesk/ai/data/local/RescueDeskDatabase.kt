package com.rescuedesk.ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        GuideEntity::class,
        GuideFtsEntity::class,
        FamilyPlanEntity::class,
        EmergencyContactEntity::class,
        GoBagItemEntity::class
    ],
    // v2: GoBagItemEntity gained labelFil (FR-07 bilingual checklist); category
    // values switched from display names to stable keys. Unreleased app, so the
    // one-time reset of dev test data is acceptable — go-bag defaults reseed.
    version = 2,
    exportSchema = false
)
abstract class RescueDeskDatabase : RoomDatabase() {

    abstract fun guideDao(): GuideDao

    abstract fun familyPlanDao(): FamilyPlanDao

    abstract fun contactDao(): ContactDao

    abstract fun goBagDao(): GoBagDao

    companion object {
        fun create(context: Context): RescueDeskDatabase =
            Room.databaseBuilder(context, RescueDeskDatabase::class.java, "rescuedesk.db")
                // Scaffold convenience only. Replace with real migrations before any
                // pilot build; destructive migration of household data is prohibited (PRD §5.12).
                .fallbackToDestructiveMigration()
                .build()
    }
}
