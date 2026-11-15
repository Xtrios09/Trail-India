package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TrailEntity::class, RecordedHikeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TrailIndiaDatabase : RoomDatabase() {
    abstract fun trailDao(): TrailDao
    abstract fun recordedHikeDao(): RecordedHikeDao

    companion object {
        @Volatile
        private var instance: TrailIndiaDatabase? = null

        fun getInstance(context: Context): TrailIndiaDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrailIndiaDatabase::class.java,
                    "trailindia_offline.db"
                ).build().also { instance = it }
            }
        }
    }
}
