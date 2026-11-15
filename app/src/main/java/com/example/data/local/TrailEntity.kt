package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Difficulty
import com.example.domain.model.GeoPointData
import com.example.domain.model.RouteType
import com.example.domain.model.Trail
import com.example.domain.model.TrailDataSource
import com.example.domain.model.TrailWaypoint
import com.example.domain.model.TrekRegion
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "trails")
data class TrailEntity(
    @PrimaryKey val id: String,
    val name: String,
    val regionName: String,
    val state: String,
    val area: String,
    val difficultyName: String,
    val routeTypeName: String,
    val distanceKm: Double,
    val elevationGainM: Int,
    val maxElevationM: Int,
    val minElevationM: Int,
    val durationMinutes: Int,
    val bestSeason: String,
    val description: String,
    val highlightsJson: String,
    val trailheadName: String,
    val trailheadLat: Double,
    val trailheadLon: Double,
    val pathPointsJson: String,
    val waypointsJson: String,
    val dataSourceName: String,
    val osmAttributionNote: String,
    val isSaved: Boolean = false,
    val isOfflineDownloaded: Boolean = false,
    val offlineTileCount: Int = 0,
    val offlineSizeBytes: Long = 0L,
    val downloadedAtEpochMs: Long? = null
)

private val jsonCodec = Json { ignoreUnknownKeys = true }

fun TrailEntity.toDomain(): Trail {
    val region = TrekRegion.entries.find { it.name == regionName } ?: TrekRegion.HIMALAYAS
    val difficulty = Difficulty.entries.find { it.name == difficultyName } ?: Difficulty.MODERATE
    val routeType = RouteType.entries.find { it.name == routeTypeName } ?: RouteType.OUT_AND_BACK
    val dataSource = TrailDataSource.entries.find { it.name == dataSourceName } ?: TrailDataSource.CURATED_ATLAS

    val highlights = runCatching {
        jsonCodec.decodeFromString<List<String>>(highlightsJson)
    }.getOrDefault(emptyList())

    val pathPoints = runCatching {
        jsonCodec.decodeFromString<List<GeoPointData>>(pathPointsJson)
    }.getOrDefault(emptyList())

    val waypoints = runCatching {
        jsonCodec.decodeFromString<List<TrailWaypoint>>(waypointsJson)
    }.getOrDefault(emptyList())

    return Trail(
        id = id,
        name = name,
        region = region,
        state = state,
        area = area,
        difficulty = difficulty,
        routeType = routeType,
        distanceKm = distanceKm,
        elevationGainM = elevationGainM,
        maxElevationM = maxElevationM,
        minElevationM = minElevationM,
        durationMinutes = durationMinutes,
        bestSeason = bestSeason,
        description = description,
        highlights = highlights,
        trailheadName = trailheadName,
        trailheadLat = trailheadLat,
        trailheadLon = trailheadLon,
        pathPoints = pathPoints,
        waypoints = waypoints,
        dataSource = dataSource,
        osmAttributionNote = osmAttributionNote,
        isSaved = isSaved,
        isOfflineDownloaded = isOfflineDownloaded,
        offlineTileCount = offlineTileCount,
        offlineSizeBytes = offlineSizeBytes,
        downloadedAtEpochMs = downloadedAtEpochMs
    )
}

fun Trail.toEntity(): TrailEntity = TrailEntity(
    id = id,
    name = name,
    regionName = region.name,
    state = state,
    area = area,
    difficultyName = difficulty.name,
    routeTypeName = routeType.name,
    distanceKm = distanceKm,
    elevationGainM = elevationGainM,
    maxElevationM = maxElevationM,
    minElevationM = minElevationM,
    durationMinutes = durationMinutes,
    bestSeason = bestSeason,
    description = description,
    highlightsJson = jsonCodec.encodeToString(highlights),
    trailheadName = trailheadName,
    trailheadLat = trailheadLat,
    trailheadLon = trailheadLon,
    pathPointsJson = jsonCodec.encodeToString(pathPoints),
    waypointsJson = jsonCodec.encodeToString(waypoints),
    dataSourceName = dataSource.name,
    osmAttributionNote = osmAttributionNote,
    isSaved = isSaved,
    isOfflineDownloaded = isOfflineDownloaded,
    offlineTileCount = offlineTileCount,
    offlineSizeBytes = offlineSizeBytes,
    downloadedAtEpochMs = downloadedAtEpochMs
)
