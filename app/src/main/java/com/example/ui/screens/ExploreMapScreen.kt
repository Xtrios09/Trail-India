package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.Difficulty
import com.example.domain.model.GeoBounds
import com.example.domain.model.GeoUtils
import com.example.domain.model.MapLayerStyle
import com.example.domain.model.OfflineZoomPreset
import com.example.domain.model.Trail
import com.example.domain.model.TrailSortOption
import com.example.domain.model.TrekRegion
import com.example.ui.components.DifficultyChip
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.OsmMapComposable
import com.example.ui.components.RegionSelectorOverlay
import com.example.ui.components.SelectorFrameSize
import com.example.ui.components.TrailPhotoImage
import com.example.ui.components.WeatherOverlayCard
import com.example.ui.components.rememberTrailHaptics
import com.example.ui.theme.ForestDeep
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.TrailIndiaUiState
import kotlin.math.roundToInt

@Composable
fun ExploreMapScreen(
    uiState: TrailIndiaUiState,
    initialFocusRegion: TrekRegion?,
    initialOpenRegionSelector: Boolean = false,
    initialFocusLat: Double? = null,
    initialFocusLon: Double? = null,
    initialFocusZoom: Double? = null,
    onSearchQueryChange: (String) -> Unit,
    onSubmitSearch: (String) -> Unit,
    onSelectRegion: (TrekRegion?) -> Unit,
    onSelectState: (String?) -> Unit,
    onSelectDifficulty: (Difficulty?) -> Unit,
    onSelectSort: (TrailSortOption) -> Unit,
    onToggleSavedOnly: () -> Unit,
    onClearFilters: () -> Unit,
    onSelectMapTrail: (String?) -> Unit,
    onStartFollowingTrail: (String) -> Unit,
    onStopFollowingTrail: () -> Unit,
    onToggleSaveTrail: (String) -> Unit,
    onOpenTrailDetail: (Trail) -> Unit,
    onSelectLayerStyle: (MapLayerStyle) -> Unit,
    onToggleOfflineMapMode: (Boolean) -> Unit,
    onTogglePeakLabels: (Boolean) -> Unit,
    onToggleWeatherOverlay: (Boolean) -> Unit,
    onRefreshWeather: () -> Unit,
    onSyncOsmAt: (Double, Double) -> Unit,
    onDownloadManualRegion: (String, GeoBounds, OfflineZoomPreset) -> Unit = { _, _, _ -> },
    onRequestLocationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTrail = uiState.allTrails.find { it.id == uiState.selectedMapTrailId }
        ?: uiState.filteredTrails.firstOrNull()?.trail
    val followingTrail = uiState.allTrails.find { it.id == uiState.followingTrailId }

    var cameraLat by remember(initialFocusRegion, initialFocusLat, initialFocusLon) {
        mutableDoubleStateOf(
            initialFocusLat
                ?: followingTrail?.trailheadLat
                ?: initialFocusRegion?.centerLat
                ?: selectedTrail?.trailheadLat
                ?: 22.9734
        )
    }
    var cameraLon by remember(initialFocusRegion, initialFocusLat, initialFocusLon) {
        mutableDoubleStateOf(
            initialFocusLon
                ?: followingTrail?.trailheadLon
                ?: initialFocusRegion?.centerLon
                ?: selectedTrail?.trailheadLon
                ?: 78.6569
        )
    }
    var cameraZoom by remember(initialFocusRegion, initialFocusLat, initialFocusLon, initialFocusZoom) {
        mutableDoubleStateOf(
            when {
                initialFocusZoom != null -> initialFocusZoom
                followingTrail != null -> 13.5
                initialFocusRegion != null -> initialFocusRegion.defaultZoom
                selectedTrail != null -> 12.2
                else -> 5.0
            }
        )
    }
    var cameraRequestKey by remember { mutableIntStateOf(0) }
    var fitTrailBoundsOnCamera by remember(initialFocusRegion, initialFocusLat, initialFocusLon) {
        mutableStateOf(followingTrail != null && initialFocusLat == null)
    }
    var pendingCenterOnGps by remember { mutableStateOf(false) }
    var lastMapCenterLat by remember { mutableDoubleStateOf(cameraLat) }
    var lastMapCenterLon by remember { mutableDoubleStateOf(cameraLon) }

    var isRegionSelectMode by remember(initialOpenRegionSelector) {
        mutableStateOf(initialOpenRegionSelector)
    }
    var frameSize by remember { mutableStateOf(SelectorFrameSize.MEDIUM) }
    var zoomPreset by remember { mutableStateOf(OfflineZoomPreset.TRAIL) }
    var selectionBounds by remember { mutableStateOf<GeoBounds?>(null) }
    var wasDownloadingRegion by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.regionDownloadProgress) {
        val inProgress = uiState.regionDownloadProgress != null
        if (wasDownloadingRegion && !inProgress && isRegionSelectMode) {
            isRegionSelectMode = false
        }
        wasDownloadingRegion = inProgress
    }

    LaunchedEffect(initialFocusLat, initialFocusLon, initialFocusZoom) {
        if (initialFocusLat != null && initialFocusLon != null) {
            pendingCenterOnGps = false
            fitTrailBoundsOnCamera = false
            cameraLat = initialFocusLat
            cameraLon = initialFocusLon
            cameraZoom = initialFocusZoom ?: 12.2
            cameraRequestKey++
        }
    }

    LaunchedEffect(uiState.liveGps.latitude, uiState.liveGps.longitude, pendingCenterOnGps) {
        val lat = uiState.liveGps.latitude
        val lon = uiState.liveGps.longitude
        if (pendingCenterOnGps && lat != null && lon != null) {
            pendingCenterOnGps = false
            fitTrailBoundsOnCamera = false
            cameraLat = lat
            cameraLon = lon
            cameraZoom = 15.0
            cameraRequestKey++
        }
    }

    var showLayerMenu by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val haptics = rememberTrailHaptics()
    val focusManager = LocalFocusManager.current

    Box(modifier = modifier.fillMaxSize()) {
        OsmMapComposable(
            modifier = Modifier.fillMaxSize(),
            trails = uiState.filteredTrails.map { it.trail },
            selectedTrail = selectedTrail,
            followingTrail = followingTrail,
            recordedPoints = uiState.recorderState.trackPoints,
            osmPeaks = uiState.liveOsmPeaks,
            showPeakLabels = uiState.showPeakLabels,
            liveGps = uiState.liveGps,
            layerStyle = uiState.mapLayerStyle,
            isOfflineMapMode = uiState.isOfflineMapMode,
            cameraCenterLat = cameraLat,
            cameraCenterLon = cameraLon,
            cameraZoom = cameraZoom,
            cameraRequestKey = cameraRequestKey,
            fitTrailBounds = fitTrailBoundsOnCamera,
            cachedRegions = uiState.offlineRegions,
            isRegionSelectMode = isRegionSelectMode,
            selectionBoxHorizontalFraction = frameSize.horizontalFraction,
            selectionBoxVerticalFraction = frameSize.verticalFraction,
            onSelectionBoundsChanged = { selectionBounds = it },
            onTrailSelected = { trail ->
                if (!isRegionSelectMode) {
                    pendingCenterOnGps = false
                    fitTrailBoundsOnCamera = true
                    onSelectMapTrail(trail.id)
                    cameraLat = trail.trailheadLat
                    cameraLon = trail.trailheadLon
                    cameraZoom = 12.5
                    cameraRequestKey++
                }
            },
            onMapCenterChanged = { lat, lon ->
                lastMapCenterLat = lat
                lastMapCenterLon = lon
            }
        )

        if (isRegionSelectMode) {
            RegionSelectorOverlay(
                selectionBounds = selectionBounds,
                frameSize = frameSize,
                zoomPreset = zoomPreset,
                layerStyle = uiState.mapLayerStyle,
                downloadProgress = uiState.regionDownloadProgress,
                suggestedRegionName = selectedTrail?.area ?: uiState.filterState.selectedRegion?.displayName,
                onFrameSizeSelected = { frameSize = it },
                onZoomPresetSelected = { zoomPreset = it },
                onDownloadClick = { name ->
                    val bounds = selectionBounds ?: return@RegionSelectorOverlay
                    onDownloadManualRegion(name, bounds, zoomPreset)
                },
                onDismiss = { isRegionSelectMode = false }
            )
        } else {
        // Top Floating Search + Filter Bar
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(99.dp),
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    TextField(
                        value = uiState.filterState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                "Search trails or regions…",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.filterState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    haptics.tap()
                                    onSearchQueryChange("")
                                }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                haptics.confirm()
                                onSubmitSearch(uiState.filterState.searchQuery)
                                focusManager.clearFocus()
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                Surface(
                    shape = CircleShape,
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    IconButton(
                        onClick = {
                            haptics.tap()
                            showFilterSheet = true
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("map_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = "Open trail filters",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Active Follow-Along GPS HUD
            if (followingTrail != null) {
                val distToStart = if (uiState.liveGps.latitude != null && uiState.liveGps.longitude != null) {
                    GeoUtils.haversineKm(
                        uiState.liveGps.latitude,
                        uiState.liveGps.longitude,
                        followingTrail.trailheadLat,
                        followingTrail.trailheadLon
                    )
                } else null

                val nearestPathDistM = if (uiState.liveGps.latitude != null && uiState.liveGps.longitude != null) {
                    followingTrail.pathPoints.minOfOrNull { pt ->
                        GeoUtils.haversineKm(
                            uiState.liveGps.latitude,
                            uiState.liveGps.longitude,
                            pt.lat,
                            pt.lon
                        ) * 1000.0
                    }
                } else null

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ForestDeep.copy(alpha = 0.94f),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NAVIGATING • ${followingTrail.name}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF74C69D),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val navSub = buildString {
                                if (distToStart != null) {
                                    append("${String.format("%.1f", distToStart)} km to trailhead")
                                } else {
                                    append("Acquiring GPS signal…")
                                }
                                if (nearestPathDistM != null) {
                                    append("  •  ${nearestPathDistM.roundToInt()} m from path")
                                }
                                append("  •  Hdg ${uiState.liveGps.compassAzimuthDeg.roundToInt()}°")
                            }
                            Text(
                                text = navSub,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = Color.White
                            )
                        }
                        IconButton(onClick = {
                            haptics.heavyAction()
                            onStopFollowingTrail()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Stop trail navigation",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            WeatherOverlayCard(
                weatherState = uiState.selectedTrailWeather,
                visible = uiState.showWeatherOverlay,
                onRefresh = onRefreshWeather,
                onHideOverlay = { onToggleWeatherOverlay(false) }
            )
        }

        // Right Floating Map Controls
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            Box {
                SmallFloatingActionButton(
                    onClick = {
                        haptics.tap()
                        showLayerMenu = true
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Rounded.Layers, contentDescription = "Map layers")
                }

                DropdownMenu(
                    expanded = showLayerMenu,
                    onDismissRequest = { showLayerMenu = false }
                ) {
                    MapLayerStyle.entries.forEach { style ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = if (uiState.mapLayerStyle == style) "✓ ${style.label}" else style.label,
                                        fontWeight = if (uiState.mapLayerStyle == style) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = style.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                haptics.tap()
                                onSelectLayerStyle(style)
                                showLayerMenu = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (uiState.isOfflineMapMode) "✓ Offline Cache Only" else "Offline Cache Only"
                            )
                        },
                        leadingIcon = { Icon(Icons.Rounded.CloudOff, contentDescription = null) },
                        onClick = {
                            haptics.tap()
                            onToggleOfflineMapMode(!uiState.isOfflineMapMode)
                            showLayerMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (uiState.showPeakLabels) "✓ Show OSM Peak Markers" else "Show OSM Peak Markers"
                            )
                        },
                        leadingIcon = { Icon(Icons.Rounded.Terrain, contentDescription = null) },
                        onClick = {
                            haptics.tap()
                            onTogglePeakLabels(!uiState.showPeakLabels)
                            showLayerMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (uiState.showWeatherOverlay) "✓ Show Trail Weather" else "Show Trail Weather"
                            )
                        },
                        leadingIcon = { Icon(Icons.Rounded.Thermostat, contentDescription = null) },
                        onClick = {
                            haptics.tap()
                            onToggleWeatherOverlay(!uiState.showWeatherOverlay)
                            showLayerMenu = false
                        }
                    )
                }
            }

            SmallFloatingActionButton(
                onClick = {
                    haptics.confirm()
                    val lat = selectedTrail?.trailheadLat ?: lastMapCenterLat
                    val lon = selectedTrail?.trailheadLon ?: lastMapCenterLon
                    onSyncOsmAt(lat, lon)
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                if (uiState.isSyncingOsm) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.Sync, contentDescription = "Sync nearby OpenStreetMap trails")
                }
            }

            SmallFloatingActionButton(
                onClick = {
                    haptics.tap()
                    isRegionSelectMode = true
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("map_select_region_button")
            ) {
                Icon(Icons.Rounded.CropFree, contentDescription = "Select offline map region")
            }

            FloatingActionButton(
                onClick = {
                    haptics.tap()
                    val gps = uiState.liveGps
                    if (!gps.hasLocationPermission) {
                        pendingCenterOnGps = true
                        onRequestLocationPermission()
                    } else if (gps.latitude != null && gps.longitude != null) {
                        pendingCenterOnGps = false
                        fitTrailBoundsOnCamera = false
                        cameraLat = gps.latitude
                        cameraLon = gps.longitude
                        cameraZoom = 15.0
                        cameraRequestKey++
                    } else {
                        pendingCenterOnGps = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("map_recenter_gps_button")
            ) {
                Icon(Icons.Rounded.MyLocation, contentDescription = "Center map on GPS")
            }
        }

        // Bottom Trail Preview Card + Attribution
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selectedTrail != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTrailDetail(selectedTrail) }
                        .testTag("map_preview_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrailPhotoImage(
                                imageUrl = uiState.trailImageUrls[selectedTrail.id],
                                fallbackDrawable = selectedTrail.region.bannerDrawable,
                                contentDescription = selectedTrail.name,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    DifficultyChip(difficulty = selectedTrail.difficulty)
                                    Text(
                                        text = selectedTrail.state,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedTrail.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${selectedTrail.distanceKm} km • ${GeoUtils.formatDuration(selectedTrail.durationMinutes)}" +
                                        if (selectedTrail.maxElevationM > 0) " • ${selectedTrail.maxElevationM} m" else "",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = {
                                haptics.confirm()
                                onToggleSaveTrail(selectedTrail.id)
                            }) {
                                Icon(
                                    imageVector = if (selectedTrail.isSaved) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Save trail",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    haptics.tap()
                                    onOpenTrailDetail(selectedTrail)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Rounded.Explore,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Route Details")
                            }
                            Button(
                                onClick = {
                                    haptics.confirm()
                                    if (!uiState.liveGps.hasLocationPermission) {
                                        onRequestLocationPermission()
                                    }
                                    pendingCenterOnGps = false
                                    fitTrailBoundsOnCamera = true
                                    onStartFollowingTrail(selectedTrail.id)
                                    cameraLat = selectedTrail.trailheadLat
                                    cameraLon = selectedTrail.trailheadLon
                                    cameraZoom = 13.5
                                    cameraRequestKey++
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                            ) {
                                Icon(
                                    Icons.Rounded.Navigation,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Navigate")
                            }
                        }
                    }
                }
            }

            // Required OpenStreetMap Attribution
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ) {
                Text(
                    text = "© OpenStreetMap contributors • ${uiState.mapLayerStyle.label}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filterState = uiState.filterState,
            availableStates = uiState.availableStates,
            onSelectRegion = onSelectRegion,
            onSelectState = onSelectState,
            onSelectDifficulty = onSelectDifficulty,
            onSelectSort = onSelectSort,
            onToggleSavedOnly = onToggleSavedOnly,
            onClearAll = onClearFilters,
            onDismiss = { showFilterSheet = false }
        )
    }
}
