package com.example.data.repository

import android.content.Context
import com.example.data.catalog.VerifiedIndianTrailsCatalog
import com.example.data.local.AppPreferencesRepository
import com.example.data.local.RecordedHikeDao
import com.example.data.local.TrailDao
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.offline.OfflineDownloadProgress
import com.example.data.offline.OfflineDownloadResult
import com.example.data.offline.OsmTileOfflineManager
import com.example.data.remote.OpenMeteoWeatherClient
import com.example.data.remote.OverpassOsmClient
import com.example.data.remote.TrailImageLoader
import com.example.data.remote.WikimediaTrailImageClient
import com.example.domain.model.GeoBounds
import com.example.domain.model.MapLayerStyle
import com.example.domain.model.OfflineMapRegion
import com.example.domain.model.OfflineZoomPreset
import com.example.domain.model.OsmPeakMarker
import com.example.domain.model.RecordedHike
import com.example.domain.model.Trail
import com.example.domain.model.TrailWeather
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class TrailRepository(
    private val context: Context,
    private val trailDao: TrailDao,
    private val recordedHikeDao: RecordedHikeDao,
    private val preferencesRepository: AppPreferencesRepository,
    private val overpassClient: OverpassOsmClient = OverpassOsmClient(),
    private val weatherClient: OpenMeteoWeatherClient = OpenMeteoWeatherClient(),
    private val imageClient: WikimediaTrailImageClient = WikimediaTrailImageClient()
) {
    val allTrails: Flow<List<Trail>> = trailDao.observeAllTrails().map { list ->
        list.map { it.toDomain() }
    }

    val recordedHikes: Flow<List<RecordedHike>> = recordedHikeDao.observeAllHikes().map { list ->
        list.map { it.toDomain() }
    }

    val mapLayerStyle: Flow<MapLayerStyle> = preferencesRepository.mapLayerStyle
    val isOfflineMapMode: Flow<Boolean> = preferencesRepository.isOfflineMapMode
    val showPeakLabels: Flow<Boolean> = preferencesRepository.showPeakLabels
    val showWeatherOverlay: Flow<Boolean> = preferencesRepository.showWeatherOverlay
    val searchHistory: Flow<List<String>> = preferencesRepository.searchHistory
    val trailImageUrls: Flow<Map<String, String>> = preferencesRepository.trailImageUrls
    val offlineRegions: Flow<List<OfflineMapRegion>> = preferencesRepository.offlineRegions

    fun observeTrailById(trailId: String): Flow<Trail?> =
        trailDao.observeTrailById(trailId).map { it?.toDomain() }

    suspend fun ensureSeededCatalog() {
        val seedEntities = VerifiedIndianTrailsCatalog.allTrails.map { it.toEntity() }
        trailDao.insertIfMissing(seedEntities)
    }

    suspend fun toggleSavedTrail(trailId: String) {
        val current = trailDao.getTrailById(trailId) ?: return
        trailDao.updateSavedStatus(trailId, !current.isSaved)
    }

    suspend fun downloadTrailForOffline(
        trail: Trail,
        layerStyle: MapLayerStyle,
        onProgress: (OfflineDownloadProgress) -> Unit
    ): Result<OfflineDownloadResult> {
        val result = OsmTileOfflineManager.downloadTrailTiles(
            context = context,
            trail = trail,
            layerStyle = layerStyle,
            onProgress = onProgress
        )
        result.onSuccess { downloaded ->
            trailDao.updateOfflineBundle(
                trailId = trail.id,
                isDownloaded = true,
                tileCount = downloaded.tileCount,
                sizeBytes = downloaded.totalBytes,
                downloadedAtMs = System.currentTimeMillis()
            )
            // Automatically bookmark downloaded trails for quick offline access
            trailDao.updateSavedStatus(trail.id, true)
            // Prefetch real trail photograph into Coil disk cache for offline viewing
            val cachedUrl = runCatching { trailImageUrls.first()[trail.id] }.getOrNull()
                ?: imageClient.resolveSingleTrailImage(trail)?.also { url ->
                    preferencesRepository.saveTrailImageUrls(mapOf(trail.id to url))
                }
            if (!cachedUrl.isNullOrBlank()) {
                TrailImageLoader.prefetch(context, cachedUrl)
            }
        }
        return result
    }

    suspend fun removeOfflineBundle(trailId: String) {
        trailDao.updateOfflineBundle(
            trailId = trailId,
            isDownloaded = false,
            tileCount = 0,
            sizeBytes = 0L,
            downloadedAtMs = null
        )
    }

    suspend fun downloadManualRegion(
        name: String,
        bounds: GeoBounds,
        zoomPreset: OfflineZoomPreset,
        layerStyle: MapLayerStyle,
        onProgress: (OfflineDownloadProgress) -> Unit
    ): Result<OfflineMapRegion> {
        val regionId = "region_${UUID.randomUUID().toString().take(8)}"
        val result = OsmTileOfflineManager.downloadBoundingBoxTiles(
            context = context,
            regionId = regionId,
            bounds = bounds,
            minZoom = zoomPreset.minZoom,
            maxZoom = zoomPreset.maxZoom,
            layerStyle = layerStyle,
            onProgress = onProgress
        )
        return result.map { downloaded ->
            val region = OfflineMapRegion(
                id = regionId,
                name = name.trim().ifEmpty { "Custom Map Area" },
                bounds = bounds,
                minZoom = zoomPreset.minZoom,
                maxZoom = zoomPreset.maxZoom,
                layerStyleName = layerStyle.label,
                tileCount = downloaded.tileCount,
                sizeBytes = downloaded.totalBytes,
                downloadedAtEpochMs = System.currentTimeMillis()
            )
            preferencesRepository.addOfflineRegion(region)
            region
        }
    }

    suspend fun removeOfflineRegion(regionId: String) =
        preferencesRepository.removeOfflineRegion(regionId)

    suspend fun clearOfflineRegions() =
        preferencesRepository.clearOfflineRegions()

    suspend fun syncLiveOsmTrails(
        centerLat: Double,
        centerLon: Double
    ): Result<Pair<Int, List<OsmPeakMarker>>> {
        val result = overpassClient.fetchNearbyTrailsAndPeaks(centerLat, centerLon)
        return result.map { queryResult ->
            if (queryResult.trails.isNotEmpty()) {
                trailDao.insertIfMissing(queryResult.trails.map { it.toEntity() })
            }
            queryResult.trails.size to queryResult.peaks
        }
    }

    suspend fun saveRecordedHike(hike: RecordedHike): Long {
        return recordedHikeDao.insertHike(hike.toEntity())
    }

    suspend fun deleteRecordedHike(hikeId: Long) {
        recordedHikeDao.deleteHikeById(hikeId)
    }

    suspend fun refreshTrailImages(trails: List<Trail>) {
        val current = runCatching { trailImageUrls.first() }.getOrDefault(emptyMap())
        val updated = imageClient.resolveCuratedTrailAndRegionImages(trails, current)
        if (updated.size > current.size) {
            preferencesRepository.saveTrailImageUrls(updated)
        }
    }

    suspend fun fetchTrailWeather(
        trail: Trail,
        forceRefresh: Boolean = false
    ): Result<TrailWeather> = weatherClient.fetchCurrentTrailWeather(trail, forceRefresh)

    fun getCachedTrailWeather(trailId: String): TrailWeather? =
        weatherClient.getCachedWeather(trailId, maxAgeMs = Long.MAX_VALUE)

    suspend fun setMapLayerStyle(style: MapLayerStyle) = preferencesRepository.setMapLayerStyle(style)
    suspend fun setOfflineMapMode(enabled: Boolean) = preferencesRepository.setOfflineMapMode(enabled)
    suspend fun setShowPeakLabels(enabled: Boolean) = preferencesRepository.setShowPeakLabels(enabled)
    suspend fun setShowWeatherOverlay(enabled: Boolean) = preferencesRepository.setShowWeatherOverlay(enabled)
    suspend fun addSearchQuery(query: String) = preferencesRepository.addSearchQuery(query)
    suspend fun removeSearchQuery(query: String) = preferencesRepository.removeSearchQuery(query)
    suspend fun clearSearchHistory() = preferencesRepository.clearSearchHistory()

    fun getTotalTileCacheSizeBytes(): Long = OsmTileOfflineManager.getCacheSizeBytes(context)

    fun clearAllTileCache(): Boolean = OsmTileOfflineManager.clearTileCache(context)
}
