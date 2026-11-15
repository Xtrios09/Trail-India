package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.domain.model.TrekRegion
import com.example.ui.components.rememberTrailHaptics
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.ExploreMapScreen
import com.example.ui.screens.OfflineLibraryScreen
import com.example.ui.screens.RecordHikeScreen
import com.example.ui.screens.TrailDetailScreen
import com.example.ui.theme.TrailIndiaTheme
import com.example.ui.viewmodel.TrailIndiaViewModel
import kotlinx.serialization.Serializable

@Serializable
data object DiscoverRoute

@Serializable
data class MapRoute(
    val regionName: String? = null,
    val openRegionSelector: Boolean = false,
    val focusLat: Double? = null,
    val focusLon: Double? = null,
    val focusZoom: Double? = null
)

@Serializable
data object RecordRoute

@Serializable
data object OfflineRoute

@Serializable
data class TrailDetailRoute(val trailId: String)

private data class TopLevelNavItem<T : Any>(
    val route: T,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrailIndiaTheme {
                TrailIndiaApp()
            }
        }
    }
}

@Composable
fun TrailIndiaApp(viewModel: TrailIndiaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = rememberTrailHaptics()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onLocationPermissionResult(granted)
    }

    val requestLocationPermissions: () -> Unit = {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.consumeStatusMessage()
    }

    val topLevelItems = remember {
        listOf(
            TopLevelNavItem(
                route = DiscoverRoute,
                label = "Discover",
                selectedIcon = Icons.Rounded.Explore,
                unselectedIcon = Icons.Outlined.Explore,
                testTag = "nav_discover"
            ),
            TopLevelNavItem(
                route = MapRoute(),
                label = "Map",
                selectedIcon = Icons.Rounded.Map,
                unselectedIcon = Icons.Outlined.Map,
                testTag = "nav_map"
            ),
            TopLevelNavItem(
                route = RecordRoute,
                label = "Record",
                selectedIcon = Icons.Rounded.RadioButtonChecked,
                unselectedIcon = Icons.Outlined.RadioButtonChecked,
                testTag = "nav_record"
            ),
            TopLevelNavItem(
                route = OfflineRoute,
                label = "Offline",
                selectedIcon = Icons.Rounded.CloudDownload,
                unselectedIcon = Icons.Outlined.CloudDownload,
                testTag = "nav_offline"
            )
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isDetailScreen = currentDestination?.hasRoute<TrailDetailRoute>() == true

    val navigateToTopLevel: (Any) -> Unit = { targetRoute ->
        haptics.tap()
        navController.navigate(targetRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 840.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useNavRail && !isDetailScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        topLevelItems.forEach { item ->
                            val selected = currentDestination?.hasRoute(item.route::class) == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail && !isDetailScreen) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        topLevelItems.forEach { item ->
                            val selected = currentDestination?.hasRoute(item.route::class) == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = DiscoverRoute
                    ) {
                        composable<DiscoverRoute> {
                            DiscoverScreen(
                                uiState = uiState,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onSubmitSearch = viewModel::submitSearchQuery,
                                onRemoveSearchHistory = viewModel::removeSearchHistoryItem,
                                onClearSearchHistory = viewModel::clearSearchHistory,
                                onSelectRegion = viewModel::selectRegion,
                                onSelectState = viewModel::selectState,
                                onSelectDifficulty = viewModel::selectDifficulty,
                                onSelectSort = viewModel::setSortOption,
                                onToggleNearby = viewModel::toggleNearbyOnly,
                                onToggleSavedOnly = viewModel::toggleSavedOnly,
                                onClearFilters = viewModel::clearAllFilters,
                                onRequestLocationPermission = requestLocationPermissions,
                                onSyncLiveOsm = {
                                    val lat = uiState.liveGps.latitude
                                        ?: uiState.filterState.selectedRegion?.centerLat
                                        ?: uiState.filteredTrails.firstOrNull()?.trail?.trailheadLat
                                        ?: 32.2425
                                    val lon = uiState.liveGps.longitude
                                        ?: uiState.filterState.selectedRegion?.centerLon
                                        ?: uiState.filteredTrails.firstOrNull()?.trail?.trailheadLon
                                        ?: 76.3323
                                    viewModel.syncLiveOsmAround(lat, lon)
                                },
                                onOpenTrailDetail = { trail ->
                                    if (uiState.filterState.searchQuery.isNotBlank()) {
                                        viewModel.submitSearchQuery()
                                    }
                                    viewModel.selectMapTrail(trail.id)
                                    navController.navigate(TrailDetailRoute(trail.id))
                                },
                                onToggleSaveTrail = viewModel::toggleSaveTrail,
                                onOpenRegionOnMap = { region ->
                                    viewModel.selectRegion(region)
                                    navController.navigate(MapRoute(regionName = region.name))
                                }
                            )
                        }

                        composable<MapRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<MapRoute>()
                            val region = route.regionName?.let { name ->
                                TrekRegion.entries.find { it.name == name }
                            }
                            ExploreMapScreen(
                                uiState = uiState,
                                initialFocusRegion = region,
                                initialOpenRegionSelector = route.openRegionSelector,
                                initialFocusLat = route.focusLat,
                                initialFocusLon = route.focusLon,
                                initialFocusZoom = route.focusZoom,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onSubmitSearch = viewModel::submitSearchQuery,
                                onSelectRegion = viewModel::selectRegion,
                                onSelectState = viewModel::selectState,
                                onSelectDifficulty = viewModel::selectDifficulty,
                                onSelectSort = viewModel::setSortOption,
                                onToggleSavedOnly = viewModel::toggleSavedOnly,
                                onClearFilters = viewModel::clearAllFilters,
                                onSelectMapTrail = viewModel::selectMapTrail,
                                onStartFollowingTrail = viewModel::startFollowingTrail,
                                onStopFollowingTrail = viewModel::stopFollowingTrail,
                                onToggleSaveTrail = viewModel::toggleSaveTrail,
                                onOpenTrailDetail = { trail ->
                                    navController.navigate(TrailDetailRoute(trail.id))
                                },
                                onSelectLayerStyle = viewModel::setMapLayerStyle,
                                onToggleOfflineMapMode = viewModel::setOfflineMapMode,
                                onTogglePeakLabels = viewModel::setShowPeakLabels,
                                onToggleWeatherOverlay = viewModel::setShowWeatherOverlay,
                                onRefreshWeather = viewModel::refreshSelectedTrailWeather,
                                onSyncOsmAt = viewModel::syncLiveOsmAround,
                                onDownloadManualRegion = viewModel::downloadManualRegionOffline,
                                onRequestLocationPermission = requestLocationPermissions
                            )
                        }

                        composable<RecordRoute> {
                            RecordHikeScreen(
                                uiState = uiState,
                                onRequestLocationPermission = requestLocationPermissions,
                                onStartRecording = viewModel::startRecordingHike,
                                onPauseRecording = viewModel::pauseRecordingHike,
                                onResumeRecording = viewModel::resumeRecordingHike,
                                onStopAndSave = viewModel::stopAndSaveRecordedHike,
                                onDiscardRecording = viewModel::discardRecordedHike,
                                onOpenOfflineLibrary = { navigateToTopLevel(OfflineRoute) }
                            )
                        }

                        composable<OfflineRoute> {
                            OfflineLibraryScreen(
                                uiState = uiState,
                                onToggleOfflineMapMode = viewModel::setOfflineMapMode,
                                onClearTileCache = viewModel::clearAllTileCache,
                                onOpenTrailDetail = { trail ->
                                    navController.navigate(TrailDetailRoute(trail.id))
                                },
                                onDownloadTrailOffline = viewModel::downloadTrailOffline,
                                onRemoveOfflineTrail = viewModel::removeOfflineTrail,
                                onToggleSaveTrail = viewModel::toggleSaveTrail,
                                onDeleteRecordedHike = viewModel::deleteRecordedHike,
                                onOpenRegionSelector = {
                                    navController.navigate(MapRoute(openRegionSelector = true)) {
                                        launchSingleTop = true
                                    }
                                },
                                onOpenOfflineRegionOnMap = { offlineRegion ->
                                    navController.navigate(
                                        MapRoute(
                                            focusLat = offlineRegion.bounds.centerLat,
                                            focusLon = offlineRegion.bounds.centerLon,
                                            focusZoom = offlineRegion.minZoom.toDouble().coerceAtLeast(11.5)
                                        )
                                    ) {
                                        launchSingleTop = true
                                    }
                                },
                                onRemoveOfflineRegion = viewModel::removeOfflineRegion
                            )
                        }

                        composable<TrailDetailRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<TrailDetailRoute>()
                            val trailFromFlow by viewModel.observeTrail(args.trailId)
                                .collectAsStateWithLifecycle(initialValue = null)
                            val trail = trailFromFlow ?: uiState.allTrails.find { it.id == args.trailId }

                            if (trail != null) {
                                TrailDetailScreen(
                                    trail = trail,
                                    imageUrl = uiState.trailImageUrls[trail.id],
                                    downloadProgress = uiState.downloadProgressByTrailId[trail.id],
                                    onBack = { navController.popBackStack() },
                                    onToggleSave = { viewModel.toggleSaveTrail(trail.id) },
                                    onDownloadOffline = { viewModel.downloadTrailOffline(trail) },
                                    onRemoveOffline = { viewModel.removeOfflineTrail(trail.id) },
                                    onNavigateOnMap = {
                                        if (!uiState.liveGps.hasLocationPermission) {
                                            requestLocationPermissions()
                                        }
                                        viewModel.startFollowingTrail(trail.id)
                                        navController.navigate(MapRoute(regionName = trail.region.name)) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onStartRecordingOnTrail = {
                                        if (!uiState.liveGps.hasLocationPermission) {
                                            requestLocationPermissions()
                                        }
                                        viewModel.startFollowingTrail(trail.id)
                                        viewModel.startRecordingHike(trail)
                                        navigateToTopLevel(RecordRoute)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
