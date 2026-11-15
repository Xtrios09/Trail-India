package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.GeoPointData
import com.example.domain.model.RecordedHike
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "recorded_hikes")
data class RecordedHikeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val linkedTrailId: String? = null,
    val linkedTrailName: String? = null,
    val startedAtEpochMs: Long,
    val durationSeconds: Long,
    val distanceKm: Double,
    val ascentM: Int,
    val maxAltitudeM: Int,
    val avgPaceMinPerKm: Double,
    val trackPointsJson: String
)

private val hikeJson = Json { ignoreUnknownKeys = true }

fun RecordedHikeEntity.toDomain(): RecordedHike {
    val points = runCatching {
        hikeJson.decodeFromString<List<GeoPointData>>(trackPointsJson)
    }.getOrDefault(emptyList())

    return RecordedHike(
        id = id,
        title = title,
        linkedTrailId = linkedTrailId,
        linkedTrailName = linkedTrailName,
        startedAtEpochMs = startedAtEpochMs,
        durationSeconds = durationSeconds,
        distanceKm = distanceKm,
        ascentM = ascentM,
        maxAltitudeM = maxAltitudeM,
        avgPaceMinPerKm = avgPaceMinPerKm,
        trackPoints = points
    )
}

fun RecordedHike.toEntity(): RecordedHikeEntity = RecordedHikeEntity(
    id = id,
    title = title,
    linkedTrailId = linkedTrailId,
    linkedTrailName = linkedTrailName,
    startedAtEpochMs = startedAtEpochMs,
    durationSeconds = durationSeconds,
    distanceKm = distanceKm,
    ascentM = ascentM,
    maxAltitudeM = maxAltitudeM,
    avgPaceMinPerKm = avgPaceMinPerKm,
    trackPointsJson = hikeJson.encodeToString(trackPoints)
)
