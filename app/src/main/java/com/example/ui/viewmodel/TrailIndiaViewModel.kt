package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferencesRepository
import com.example.data.local.TrailIndiaDatabase
import com.example.data.location.LocationTracker
import com.example.data.offline.OfflineDownloadProgress
import com.example.data.offline.OsmTileOfflineManager
import com.example.data.repository.TrailRepository
import com.example.domain.model.Difficulty
import com.example.domain.model.GeoBounds
import com.example.domain.model.GeoPointData
import com.example.domain.model.GeoUtils
import com.example.domain.model.LiveGpsState
import com.example.domain.model.MapLayerStyle
import com.example.domain.model.OfflineMapRegion
import com.example.domain.model.OfflineZoomPreset
import com.example.domain.model.OsmPeakMarker
import com.example.domain.model.RecordedHike
import com.example.domain.model.Trail
import com.example.domain.model.TrailSortOption
import com.example.domain.model.TrailWeatherUiState
import com.example.domain.model.TrekRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class RecordingStatus {
    IDLE,
    RECORDING,
    PAUSED
}

data class TrailFilterState(
    val searchQuery: String = "",
    val selectedRegion: TrekRegion? = null,
    val selectedState: String? = null,
    val selectedDifficulty: Difficulty? = null,
    val nearbyOnly: Boolean = false,
    val savedOnly: Boolean = false,
    val sortOption: TrailSortOption = TrailSortOption.FEATURED
)

data class RecorderState(
    val status: RecordingStatus = RecordingStatus.IDLE,
    val linkedTrailId: String? = null,
    val linkedTrailName: String? = null,
    val startedAtEpochMs: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val distanceKm: Double = 0.0,
    val ascentM: Int = 0,
    val currentAltitudeM: Int = 0,
    val maxAltitudeM: Int = 0,
    val trackPoints: List<GeoPointData> = emptyList()
) {
    val avgPaceMinPerKm: Double
        get() = if (distanceKm >= 0.05 && elapsedSeconds > 0) {
            (elapsedSeconds / 60.0) / distanceKm
        } else 0.0
}

data class TrailWithDistance(
    val trail: Trail,
    val distanceFromUserKm: Double?
)

data class TrailIndiaUiState(
    val allTrails: List<Trail> = emptyList(),
    val filteredTrails: List<TrailWithDistance> = emptyList(),
    val availableStates: List<String> = emptyList(),
    val filterState: TrailFilterState = TrailFilterState(),
    val selectedMapTrailId: String? = null,
    val followingTrailId: String? = null,
    val mapLayerStyle: MapLayerStyle = MapLayerStyle.OPENTOPOMAP,
    val isOfflineMapMode: Boolean = false,
    val showPeakLabels: Boolean = true,
    val showWeatherOverlay: Boolean = true,
    val selectedTrailWeather: TrailWeatherUiState = TrailWeatherUiState.Idle,
    val liveGps: LiveGpsState = LiveGpsState(),
    val liveOsmPeaks: List<OsmPeakMarker> = emptyList(),
    val isSyncingOsm: Boolean = false,
    val downloadProgressByTrailId: Map<String, OfflineDownloadProgress> = emptyMap(),
    val offlineRegions: List<OfflineMapRegion> = emptyList(),
    val regionDownloadProgress: OfflineDownloadProgress? = null,
    val recordedHikes: List<RecordedHike> = emptyList(),
    val recorderState: RecorderState = RecorderState(),
    val searchHistory: List<String> = emptyList(),
    val trailImageUrls: Map<String, String> = emptyMap(),
    val totalTileCacheBytes: Long = 0L,
    val statusMessage: String? = null
)

class TrailIndiaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TrailIndiaDatabase.getInstance(application)
    private val preferences = AppPreferencesRepository(application)
    private val repository = TrailRepository(
        context = application,
        trailDao = database.trailDao(),
        recordedHikeDao = database.recordedHikeDao(),
        preferencesRepository = preferences
    )
    private val locationTracker = LocationTracker(application)

    private val filterStateFlow = MutableStateFlow(TrailFilterState())
    private val selectedMapTrailIdFlow = MutableStateFlow<String?>(null)
    private val followingTrailIdFlow = MutableStateFlow<String?>(null)
    private val liveGpsFlow = MutableStateFlow(LiveGpsState(hasLocationPermission = locationTracker.hasLocationPermission()))
    private val liveOsmPeaksFlow = MutableStateFlow<List<OsmPeakMarker>>(emptyList())
    private val isSyncingOsmFlow = MutableStateFlow(false)
    private val downloadProgressFlow = MutableStateFlow<Map<String, OfflineDownloadProgress>>(emptyMap())
    private val regionDownloadProgressFlow = MutableStateFlow<OfflineDownloadProgress?>(null)
    private val recorderStateFlow = MutableStateFlow(RecorderState())
    private val totalCacheBytesFlow = MutableStateFlow(0L)
    private val statusMessageFlow = MutableStateFlow<String?>(null)
    private val selectedTrailWeatherFlow = MutableStateFlow<TrailWeatherUiState>(TrailWeatherUiState.Idle)

    private var locationCollectionJob: Job? = null
    private var timerJob: Job? = null
    private var weatherFetchJob: Job? = null

    private data class CoreInputs(
        val trails: List<Trail>,
        val filter: TrailFilterState,
        val gps: LiveGpsState,
        val selectedId: String?,
        val followingId: String?
    )

    private data class MapPrefInputs(
        val layerStyle: MapLayerStyle,
        val offlineMode: Boolean,
        val showPeaks: Boolean,
        val showWeather: Boolean
    )

    private data class MapSettingsInputs(
        val layerStyle: MapLayerStyle,
        val offlineMode: Boolean,
        val showPeaks: Boolean,
        val showWeather: Boolean,
        val osmPeaks: List<OsmPeakMarker>,
        val syncingOsm: Boolean
    )

    private data class PersistenceInputs(
        val downloads: Map<String, OfflineDownloadProgress>,
        val hikes: List<RecordedHike>,
        val recorder: RecorderState,
        val cacheBytes: Long,
        val statusMsg: String?
    )

    private data class AuxInputs(
        val history: List<String>,
        val images: Map<String, String>,
        val weatherState: TrailWeatherUiState,
        val offlineRegions: List<OfflineMapRegion>,
        val regionDownloadProgress: OfflineDownloadProgress?
    )

    private val coreFlow = combine(
        repository.allTrails,
        filterStateFlow,
        liveGpsFlow,
        selectedMapTrailIdFlow,
        followingTrailIdFlow
    ) { trails, filter, gps, selectedId, followingId ->
        CoreInputs(trails, filter, gps, selectedId, followingId)
    }

    private val mapSettingsFlow = combine(
        combine(
            repository.mapLayerStyle,
            repository.isOfflineMapMode,
            repository.showPeakLabels,
            repository.showWeatherOverlay
        ) { layerStyle, offlineMode, showPeaks, showWeather ->
            MapPrefInputs(layerStyle, offlineMode, showPeaks, showWeather)
        },
        liveOsmPeaksFlow,
        isSyncingOsmFlow
    ) { prefs, osmPeaks, syncingOsm ->
        MapSettingsInputs(
            layerStyle = prefs.layerStyle,
            offlineMode = prefs.offlineMode,
            showPeaks = prefs.showPeaks,
            showWeather = prefs.showWeather,
            osmPeaks = osmPeaks,
            syncingOsm = syncingOsm
        )
    }

    private val persistenceFlow = combine(
        downloadProgressFlow,
        repository.recordedHikes,
        recorderStateFlow,
        totalCacheBytesFlow,
        statusMessageFlow
    ) { downloads, hikes, recorder, cacheBytes, statusMsg ->
        PersistenceInputs(downloads, hikes, recorder, cacheBytes, statusMsg)
    }

    private val auxFlow = combine(
        repository.searchHistory,
        repository.trailImageUrls,
        selectedTrailWeatherFlow,
        repository.offlineRegions,
        regionDownloadProgressFlow
    ) { history, images, weatherState, regions, regionProgress ->
        AuxInputs(history, images, weatherState, regions, regionProgress)
    }

    val uiState: StateFlow<TrailIndiaUiState> = combine(
        coreFlow,
        mapSettingsFlow,
        persistenceFlow,
        auxFlow
    ) { core, map, persist, aux ->
        val availableStates = core.trails.map { it.state }.distinct().sorted()
        val withDistances = core.trails.map { trail ->
            val dist = if (core.gps.latitude != null && core.gps.longitude != null) {
                GeoUtils.haversineKm(
                    core.gps.latitude,
                    core.gps.longitude,
                    trail.trailheadLat,
                    trail.trailheadLon
                )
            } else null
            TrailWithDistance(trail, dist)
        }

        val query = core.filter.searchQuery.trim().lowercase()
        val filtered = withDistances
            .filter { item ->
                val t = item.trail
                val matchesQuery = query.isEmpty() ||
                    t.name.lowercase().contains(query) ||
                    t.state.lowercase().contains(query) ||
                    t.area.lowercase().contains(query) ||
                    t.region.displayName.lowercase().contains(query)
                val matchesRegion = core.filter.selectedRegion == null || t.region == core.filter.selectedRegion
                val matchesState = core.filter.selectedState == null || t.state == core.filter.selectedState
                val matchesDiff = core.filter.selectedDifficulty == null || t.difficulty == core.filter.selectedDifficulty
                val matchesSaved = !core.filter.savedOnly || t.isSaved || t.isOfflineDownloaded
                val matchesNearby = !core.filter.nearbyOnly || (item.distanceFromUserKm != null && item.distanceFromUserKm <= 250.0)
                matchesQuery && matchesRegion && matchesState && matchesDiff && matchesSaved && matchesNearby
            }
            .let { list ->
                when (core.filter.sortOption) {
                    TrailSortOption.FEATURED -> list.sortedByDescending { it.trail.isOfflineDownloaded || it.trail.isSaved }
                    TrailSortOption.NEARBY -> list.sortedBy { it.distanceFromUserKm ?: Double.MAX_VALUE }
                    TrailSortOption.SHORTEST -> list.sortedBy { it.trail.distanceKm }
                    TrailSortOption.HIGHEST_PEAK -> list.sortedByDescending { it.trail.maxElevationM }
                }
            }

        TrailIndiaUiState(
            allTrails = core.trails,
            filteredTrails = filtered,
            availableStates = availableStates,
            filterState = core.filter,
            selectedMapTrailId = core.selectedId ?: filtered.firstOrNull()?.trail?.id,
            followingTrailId = core.followingId,
            mapLayerStyle = map.layerStyle,
            isOfflineMapMode = map.offlineMode,
            showPeakLabels = map.showPeaks,
            showWeatherOverlay = map.showWeather,
            selectedTrailWeather = aux.weatherState,
            liveGps = core.gps,
            liveOsmPeaks = map.osmPeaks,
            isSyncingOsm = map.syncingOsm,
            downloadProgressByTrailId = persist.downloads,
            offlineRegions = aux.offlineRegions,
            regionDownloadProgress = aux.regionDownloadProgress,
            recordedHikes = persist.hikes,
            recorderState = persist.recorder,
            searchHistory = aux.history,
            trailImageUrls = aux.images,
            totalTileCacheBytes = persist.cacheBytes,
            statusMessage = persist.statusMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrailIndiaUiState()
    )

    init {
        OsmTileOfflineManager.ensureOsmdroidConfigured(application)
        viewModelScope.launch {
            repository.ensureSeededCatalog()
            refreshCacheSize()
            val seeded = runCatching { repository.allTrails.first() }.getOrDefault(emptyList())
            repository.refreshTrailImages(seeded)
        }
        startLocationTrackingIfPermitted()
        observeActiveTrailForWeather()
    }

    fun observeTrail(trailId: String) = repository.observeTrailById(trailId)

    fun onLocationPermissionResult(granted: Boolean) {
        liveGpsFlow.update { it.copy(hasLocationPermission = granted) }
        if (granted) {
            startLocationTrackingIfPermitted()
        } else {
            statusMessageFlow.value = "Location permission denied. You can still browse maps and offline trails."
        }
    }

    private fun startLocationTrackingIfPermitted() {
        if (!locationTracker.hasLocationPermission()) return
        locationCollectionJob?.cancel()
        locationCollectionJob = viewModelScope.launch {
            locationTracker.observeLocationAndCompass().collect { gps ->
                liveGpsFlow.value = gps
                handleRecorderLocationUpdate(gps)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        filterStateFlow.update { it.copy(searchQuery = query) }
    }

    fun submitSearchQuery(query: String = filterStateFlow.value.searchQuery) {
        val clean = query.trim()
        if (clean.length < 2) return
        filterStateFlow.update { it.copy(searchQuery = clean) }
        viewModelScope.launch {
            repository.addSearchQuery(clean)
        }
    }

    fun removeSearchHistoryItem(query: String) {
        viewModelScope.launch {
            repository.removeSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun selectRegion(region: TrekRegion?) {
        filterStateFlow.update {
            it.copy(
                selectedRegion = if (it.selectedRegion == region) null else region,
                selectedState = null
            )
        }
    }

    fun selectState(state: String?) {
        filterStateFlow.update { it.copy(selectedState = state) }
    }

    fun selectDifficulty(difficulty: Difficulty?) {
        filterStateFlow.update {
            it.copy(selectedDifficulty = if (it.selectedDifficulty == difficulty) null else difficulty)
        }
    }

    fun toggleNearbyOnly() {
        val gps = liveGpsFlow.value
        if (gps.latitude == null) {
            statusMessageFlow.value = "Enable GPS location to filter trails near you."
        }
        filterStateFlow.update {
            val next = !it.nearbyOnly
            it.copy(
                nearbyOnly = next,
                sortOption = if (next) TrailSortOption.NEARBY else it.sortOption
            )
        }
    }

    fun toggleSavedOnly() {
        filterStateFlow.update { it.copy(savedOnly = !it.savedOnly) }
    }

    fun setSortOption(option: TrailSortOption) {
        filterStateFlow.update { it.copy(sortOption = option) }
    }

    fun clearAllFilters() {
        filterStateFlow.value = TrailFilterState()
    }

    fun selectMapTrail(trailId: String?) {
        selectedMapTrailIdFlow.value = trailId
    }

    fun startFollowingTrail(trailId: String) {
        followingTrailIdFlow.value = trailId
        selectedMapTrailIdFlow.value = trailId
        statusMessageFlow.value = "GPS Follow-Along active. Stay safe on the trail!"
    }

    fun stopFollowingTrail() {
        followingTrailIdFlow.value = null
    }

    fun toggleSaveTrail(trailId: String) {
        viewModelScope.launch {
            repository.toggleSavedTrail(trailId)
        }
    }

    fun downloadTrailOffline(trail: Trail) {
        if (downloadProgressFlow.value.containsKey(trail.id)) return
        val style = uiState.value.mapLayerStyle
        viewModelScope.launch {
            downloadProgressFlow.update { it + (trail.id to OfflineDownloadProgress(trail.id, 0, 1)) }
            val result = repository.downloadTrailForOffline(trail, style) { progress ->
                downloadProgressFlow.update { it + (trail.id to progress) }
            }
            downloadProgressFlow.update { it - trail.id }
            refreshCacheSize()
            result.fold(
                onSuccess = { res ->
                    val mb = String.format("%.1f", res.totalBytes / (1024.0 * 1024.0))
                    statusMessageFlow.value = "${trail.name} cached offline (${res.tileCount} OSM tiles • $mb MB)"
                },
                onFailure = { err ->
                    statusMessageFlow.value = "Offline tile download failed: ${err.message ?: "Check connection"}"
                }
            )
        }
    }

    fun removeOfflineTrail(trailId: String) {
        viewModelScope.launch {
            repository.removeOfflineBundle(trailId)
            statusMessageFlow.value = "Removed offline trail flag."
        }
    }

    fun downloadManualRegionOffline(
        name: String,
        bounds: GeoBounds,
        zoomPreset: OfflineZoomPreset
    ) {
        if (regionDownloadProgressFlow.value != null) return
        val style = uiState.value.mapLayerStyle
        viewModelScope.launch {
            regionDownloadProgressFlow.value = OfflineDownloadProgress("manual_region", 0, 1)
            val result = repository.downloadManualRegion(
                name = name,
                bounds = bounds,
                zoomPreset = zoomPreset,
                layerStyle = style
            ) { progress ->
                regionDownloadProgressFlow.value = progress
            }
            regionDownloadProgressFlow.value = null
            refreshCacheSize()
            result.fold(
                onSuccess = { region ->
                    val mb = String.format("%.1f", region.sizeBytes / (1024.0 * 1024.0))
                    statusMessageFlow.value =
                        "\"${region.name}\" cached offline (${region.tileCount} OSM tiles • $mb MB)"
                },
                onFailure = { err ->
                    statusMessageFlow.value =
                        "Region tile download failed: ${err.message ?: "Check connection"}"
                }
            )
        }
    }

    fun removeOfflineRegion(regionId: String) {
        viewModelScope.launch {
            repository.removeOfflineRegion(regionId)
            statusMessageFlow.value = "Removed saved offline region."
        }
    }

    fun syncLiveOsmAround(lat: Double, lon: Double) {
        if (isSyncingOsmFlow.value) return
        viewModelScope.launch {
            isSyncingOsmFlow.value = true
            val result = repository.syncLiveOsmTrails(lat, lon)
            isSyncingOsmFlow.value = false
            result.fold(
                onSuccess = { (trailCount, peaks) ->
                    liveOsmPeaksFlow.value = peaks
                    statusMessageFlow.value = if (trailCount > 0 || peaks.isNotEmpty()) {
                        "OSM Overpass: synced $trailCount trails & ${peaks.size} peaks nearby."
                    } else {
                        "OSM Overpass reachable — no extra named trails found in 25 km radius."
                    }
                    if (trailCount > 0) {
                        val updatedTrails = runCatching { repository.allTrails.first() }.getOrDefault(emptyList())
                        repository.refreshTrailImages(updatedTrails)
                    }
                },
                onFailure = {
                    statusMessageFlow.value = "Offline or Overpass busy — showing cached India trails."
                }
            )
        }
    }

    fun setMapLayerStyle(style: MapLayerStyle) {
        viewModelScope.launch { repository.setMapLayerStyle(style) }
    }

    fun setOfflineMapMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setOfflineMapMode(enabled)
            statusMessageFlow.value = if (enabled) {
                "Offline Map Mode ON — using cached OpenStreetMap tiles only."
            } else {
                "Online + Cache Map Mode ON — fetching new OSM tiles as needed."
            }
        }
    }

    fun setShowPeakLabels(enabled: Boolean) {
        viewModelScope.launch { repository.setShowPeakLabels(enabled) }
    }

    fun setShowWeatherOverlay(enabled: Boolean) {
        viewModelScope.launch { repository.setShowWeatherOverlay(enabled) }
    }

    fun refreshSelectedTrailWeather() {
        val state = uiState.value
        val activeId = state.followingTrailId ?: state.selectedMapTrailId ?: return
        val trail = state.allTrails.find { it.id == activeId } ?: return
        loadWeatherForTrail(trail = trail, forceRefresh = true, offlineMode = state.isOfflineMapMode)
    }

    private fun observeActiveTrailForWeather() {
        viewModelScope.launch {
            uiState
                .map { state ->
                    val activeId = state.followingTrailId ?: state.selectedMapTrailId
                    val trail = activeId?.let { id -> state.allTrails.find { it.id == id } }
                    Triple(trail, state.showWeatherOverlay, state.isOfflineMapMode)
                }
                .distinctUntilChanged { old, new ->
                    old.first?.id == new.first?.id &&
                        old.second == new.second &&
                        old.third == new.third
                }
                .collect { (trail, showWeather, offlineMode) ->
                    if (!showWeather || trail == null) {
                        weatherFetchJob?.cancel()
                        selectedTrailWeatherFlow.value = TrailWeatherUiState.Idle
                    } else {
                        loadWeatherForTrail(trail = trail, forceRefresh = false, offlineMode = offlineMode)
                    }
                }
        }
    }

    private fun loadWeatherForTrail(
        trail: Trail,
        forceRefresh: Boolean,
        offlineMode: Boolean
    ) {
        weatherFetchJob?.cancel()
        if (offlineMode) {
            val cached = repository.getCachedTrailWeather(trail.id)
            selectedTrailWeatherFlow.value = if (cached != null) {
                TrailWeatherUiState.Ready(cached.copy(isFromCache = true))
            } else {
                TrailWeatherUiState.Unavailable(
                    trailId = trail.id,
                    trailName = trail.name,
                    message = "Offline mode active — weather paused",
                    isOffline = true
                )
            }
            return
        }

        weatherFetchJob = viewModelScope.launch {
            if (!forceRefresh) {
                repository.getCachedTrailWeather(trail.id)?.let { cached ->
                    val age = System.currentTimeMillis() - cached.observedAtEpochMs
                    if (age <= com.example.data.remote.OpenMeteoWeatherClient.CACHE_TTL_MS) {
                        selectedTrailWeatherFlow.value = TrailWeatherUiState.Ready(cached)
                        return@launch
                    }
                }
            }
            selectedTrailWeatherFlow.value = TrailWeatherUiState.Loading(trail.id, trail.name)
            val result = repository.fetchTrailWeather(trail, forceRefresh = forceRefresh)
            result.fold(
                onSuccess = { weather ->
                    selectedTrailWeatherFlow.value = TrailWeatherUiState.Ready(weather)
                },
                onFailure = {
                    selectedTrailWeatherFlow.value = TrailWeatherUiState.Unavailable(
                        trailId = trail.id,
                        trailName = trail.name,
                        message = "Weather unavailable — tap to retry",
                        isOffline = false
                    )
                }
            )
        }
    }

    fun refreshCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            totalCacheBytesFlow.value = repository.getTotalTileCacheSizeBytes()
        }
    }

    fun clearAllTileCache() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllTileCache()
            repository.clearOfflineRegions()
            totalCacheBytesFlow.value = repository.getTotalTileCacheSizeBytes()
            statusMessageFlow.value = "Cleared cached OpenStreetMap tiles."
        }
    }

    // GPS Hike Recorder Controls
    fun startRecordingHike(linkedTrail: Trail? = null) {
        val gps = liveGpsFlow.value
        if (!gps.hasLocationPermission) {
            statusMessageFlow.value = "Grant GPS permission to record your hike."
            return
        }
        val initialPoints = if (gps.latitude != null && gps.longitude != null) {
            listOf(GeoPointData(gps.latitude, gps.longitude, gps.altitudeM ?: 0.0))
        } else emptyList()
        val startAlt = gps.altitudeM?.roundToInt() ?: 0

        recorderStateFlow.value = RecorderState(
            status = RecordingStatus.RECORDING,
            linkedTrailId = linkedTrail?.id,
            linkedTrailName = linkedTrail?.name,
            startedAtEpochMs = System.currentTimeMillis(),
            elapsedSeconds = 0L,
            distanceKm = 0.0,
            ascentM = 0,
            currentAltitudeM = startAlt,
            maxAltitudeM = startAlt,
            trackPoints = initialPoints
        )
        startTimerLoop()
    }

    fun pauseRecordingHike() {
        if (recorderStateFlow.value.status == RecordingStatus.RECORDING) {
            recorderStateFlow.update { it.copy(status = RecordingStatus.PAUSED) }
            timerJob?.cancel()
        }
    }

    fun resumeRecordingHike() {
        if (recorderStateFlow.value.status == RecordingStatus.PAUSED) {
            recorderStateFlow.update { it.copy(status = RecordingStatus.RECORDING) }
            startTimerLoop()
        }
    }

    fun stopAndSaveRecordedHike(customTitle: String? = null) {
        timerJob?.cancel()
        val current = recorderStateFlow.value
        if (current.status == RecordingStatus.IDLE) return

        val finalTitle = customTitle?.takeIf { it.isNotBlank() }
            ?: current.linkedTrailName?.let { "$it Hike" }
            ?: "India Trek Log"

        val hike = RecordedHike(
            title = finalTitle,
            linkedTrailId = current.linkedTrailId,
            linkedTrailName = current.linkedTrailName,
            startedAtEpochMs = current.startedAtEpochMs,
            durationSeconds = current.elapsedSeconds.coerceAtLeast(1L),
            distanceKm = (current.distanceKm * 100.0).roundToInt() / 100.0,
            ascentM = current.ascentM,
            maxAltitudeM = current.maxAltitudeM,
            avgPaceMinPerKm = current.avgPaceMinPerKm,
            trackPoints = current.trackPoints
        )
        recorderStateFlow.value = RecorderState()
        viewModelScope.launch {
            repository.saveRecordedHike(hike)
            statusMessageFlow.value = "Saved \"$finalTitle\" (${String.format("%.2f", hike.distanceKm)} km) locally."
        }
    }

    fun discardRecordedHike() {
        timerJob?.cancel()
        recorderStateFlow.value = RecorderState()
    }

    fun deleteRecordedHike(hikeId: Long) {
        viewModelScope.launch {
            repository.deleteRecordedHike(hikeId)
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (recorderStateFlow.value.status == RecordingStatus.RECORDING) {
                    recorderStateFlow.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1L) }
                }
            }
        }
    }

    private fun handleRecorderLocationUpdate(gps: LiveGpsState) {
        val current = recorderStateFlow.value
        if (current.status != RecordingStatus.RECORDING) return
        val lat = gps.latitude ?: return
        val lon = gps.longitude ?: return
        val alt = gps.altitudeM ?: 0.0

        val lastPt = current.trackPoints.lastOrNull()
        if (lastPt == null) {
            val altInt = alt.roundToInt()
            recorderStateFlow.update {
                it.copy(
                    currentAltitudeM = altInt,
                    maxAltitudeM = maxOf(it.maxAltitudeM, altInt),
                    trackPoints = listOf(GeoPointData(lat, lon, alt))
                )
            }
            return
        }

        val segmentKm = GeoUtils.haversineKm(lastPt.lat, lastPt.lon, lat, lon)
        // Filter GPS jitter (< 4 meters)
        if (segmentKm < 0.004) return

        val elevDelta = if (alt > 0.0 && lastPt.elevationM > 0.0) {
            (alt - lastPt.elevationM).coerceAtLeast(0.0).roundToInt()
        } else 0
        val altInt = alt.roundToInt()

        recorderStateFlow.update {
            it.copy(
                distanceKm = it.distanceKm + segmentKm,
                ascentM = it.ascentM + elevDelta,
                currentAltitudeM = if (altInt > 0) altInt else it.currentAltitudeM,
                maxAltitudeM = maxOf(it.maxAltitudeM, altInt),
                trackPoints = it.trackPoints + GeoPointData(lat, lon, alt)
            )
        }
    }

    fun consumeStatusMessage() {
        statusMessageFlow.value = null
    }

    override fun onCleared() {
        super.onCleared()
        locationCollectionJob?.cancel()
        timerJob?.cancel()
        weatherFetchJob?.cancel()
    }
}
