package com.rescuedesk.ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GuideEntity::class, GuideFtsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RescueDeskDatabase : RoomDatabase() {

    abstract fun guideDao(): GuideDao

    companion object {
        fun create(context: Context): RescueDeskDatabase =
            Room.databaseBuilder(context, RescueDeskDatabase::class.java, "rescuedesk.db")
                // Scaffold convenience only. Replace with real migrations before any
                // pilot build; destructive migration of household data is prohibited (PRD §5.12).
                .fallbackToDestructiveMigration()
                .build()
    }
}
