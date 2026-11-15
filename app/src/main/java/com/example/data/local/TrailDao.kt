package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailDao {
    @Query("SELECT * FROM trails")
    fun observeAllTrails(): Flow<List<TrailEntity>>

    @Query("SELECT * FROM trails WHERE id = :trailId")
    fun observeTrailById(trailId: String): Flow<TrailEntity?>

    @Query("SELECT * FROM trails WHERE id = :trailId")
    suspend fun getTrailById(trailId: String): TrailEntity?

    @Query("SELECT COUNT(*) FROM trails")
    suspend fun countTrails(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(trails: List<TrailEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrails(trails: List<TrailEntity>)

    @Query("UPDATE trails SET isSaved = :isSaved WHERE id = :trailId")
    suspend fun updateSavedStatus(trailId: String, isSaved: Boolean)

    @Query(
        """
        UPDATE trails
        SET isOfflineDownloaded = :isDownloaded,
            offlineTileCount = :tileCount,
            offlineSizeBytes = :sizeBytes,
            downloadedAtEpochMs = :downloadedAtMs
        WHERE id = :trailId
        """
    )
    suspend fun updateOfflineBundle(
        trailId: String,
        isDownloaded: Boolean,
        tileCount: Int,
        sizeBytes: Long,
        downloadedAtMs: Long?
    )
}
