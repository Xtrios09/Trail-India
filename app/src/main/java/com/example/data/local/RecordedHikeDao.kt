package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordedHikeDao {
    @Query("SELECT * FROM recorded_hikes ORDER BY startedAtEpochMs DESC")
    fun observeAllHikes(): Flow<List<RecordedHikeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHike(hike: RecordedHikeEntity): Long

    @Query("DELETE FROM recorded_hikes WHERE id = :hikeId")
    suspend fun deleteHikeById(hikeId: Long)
}
