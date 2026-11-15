package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.remote.WikimediaTrailImageClient
import com.example.domain.model.Difficulty
import com.example.domain.model.Trail
import com.example.domain.model.TrailSortOption
import com.example.domain.model.TrekRegion
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.TrailCard
import com.example.ui.components.TrailPhotoImage
import com.example.ui.components.rememberTrailHaptics
import com.example.ui.theme.ForestDeep
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.TrailIndiaUiState

@Composable
fun DiscoverScreen(
    uiState: TrailIndiaUiState,
    onSearchQueryChange: (String) -> Unit,
    onSubmitSearch: (String) -> Unit,
    onRemoveSearchHistory: (String) -> Unit,
    onClearSearchHistory: () -> Unit,
    onSelectRegion: (TrekRegion?) -> Unit,
    onSelectState: (String?) -> Unit,
    onSelectDifficulty: (Difficulty?) -> Unit,
    onSelectSort: (TrailSortOption) -> Unit,
    onToggleNearby: () -> Unit,
    onToggleSavedOnly: () -> Unit,
    onClearFilters: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onSyncLiveOsm: () -> Unit,
    onOpenTrailDetail: (Trail) -> Unit,
    onToggleSaveTrail: (String) -> Unit,
    onOpenRegionOnMap: (TrekRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val haptics = rememberTrailHaptics()
    val focusManager = LocalFocusManager.current

    val activeFilterCount = listOfNotNull(
        uiState.filterState.selectedRegion,
        uiState.filterState.selectedState,
        uiState.filterState.selectedDifficulty,
        if (uiState.filterState.nearbyOnly) "nearby" else null,
        if (uiState.filterState.savedOnly) "saved" else null
    ).size

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header + Search
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TrailIndia",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "OpenStreetMap Trekking Atlas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = if (uiState.isOfflineMapMode) {
                            Terracotta.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isOfflineMapMode) Icons.Rounded.CloudOff else Icons.Rounded.Public,
                                contentDescription = null,
                                tint = if (uiState.isOfflineMapMode) Terracotta else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (uiState.isOfflineMapMode) "Offline Ready" else "OSM Atlas",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.isOfflineMapMode) Terracotta else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = uiState.filterState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("discover_search_input"),
                    placeholder = { Text("Search trails, peaks, or Indian states…") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (uiState.filterState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                haptics.tap()
                                onSearchQueryChange("")
                            }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search")
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
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Recent Search History Chips
                if (uiState.searchHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    haptics.heavyAction()
                                    onClearSearchHistory()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("clear_search_history")
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(uiState.searchHistory, key = { "hist_$it" }) { pastQuery ->
                            InputChip(
                                selected = uiState.filterState.searchQuery.equals(pastQuery, ignoreCase = true),
                                onClick = {
                                    haptics.tap()
                                    onSubmitSearch(pastQuery)
                                    focusManager.clearFocus()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Remove $pastQuery",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                haptics.tap()
                                                onRemoveSearchHistory(pastQuery)
                                            }
                                    )
                                },
                                label = { Text(pastQuery) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick action pills: Filters, Nearby, Sync OSM
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = activeFilterCount > 0,
                            onClick = {
                                haptics.tap()
                                showFilterSheet = true
                            },
                            modifier = Modifier.testTag("open_filters_button"),
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.FilterList,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = {
                                Text(
                                    if (activeFilterCount > 0) "Filters ($activeFilterCount)" else "Filters"
                                )
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filterState.nearbyOnly,
                            onClick = {
                                haptics.tap()
                                if (!uiState.liveGps.hasLocationPermission) {
                                    onRequestLocationPermission()
                                }
                                onToggleNearby()
                            },
                            modifier = Modifier.testTag("nearby_chip"),
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.MyLocation,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = { Text("Nearby") }
                        )
                    }
                    item {
                        AssistChip(
                            onClick = {
                                haptics.confirm()
                                onSyncLiveOsm()
                            },
                            modifier = Modifier.testTag("sync_osm_chip"),
                            leadingIcon = {
                                if (uiState.isSyncingOsm) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.Sync,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            label = { Text("Sync OSM") }
                        )
                    }
                    items(Difficulty.entries) { diff ->
                        FilterChip(
                            selected = uiState.filterState.selectedDifficulty == diff,
                            onClick = {
                                haptics.tap()
                                onSelectDifficulty(diff)
                            },
                            label = { Text(diff.label) }
                        )
                    }
                }
            }
        }

        // Region Carousel covering all 5 requested trekking zones
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore Regions",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (uiState.filterState.selectedRegion != null) {
                        Text(
                            text = "Show All",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onSelectRegion(null) }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(TrekRegion.entries) { region ->
                        val isSelected = uiState.filterState.selectedRegion == region
                        val count = uiState.allTrails.count { it.region == region }
                        RegionBannerCard(
                            region = region,
                            imageUrl = uiState.trailImageUrls[WikimediaTrailImageClient.regionKey(region)],
                            trailCount = count,
                            isSelected = isSelected,
                            onClick = {
                                haptics.tap()
                                onSelectRegion(region)
                            },
                            onMapClick = {
                                haptics.tap()
                                onOpenRegionOnMap(region)
                            }
                        )
                    }
                }
            }
        }

        // State Quick Chips
        if (uiState.availableStates.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.filterState.selectedState == null,
                            onClick = {
                                haptics.tap()
                                onSelectState(null)
                            },
                            label = { Text("All States") }
                        )
                    }
                    items(uiState.availableStates) { stateName ->
                        FilterChip(
                            selected = uiState.filterState.selectedState == stateName,
                            onClick = {
                                haptics.tap()
                                onSelectState(if (uiState.filterState.selectedState == stateName) null else stateName)
                            },
                            label = { Text(stateName) }
                        )
                    }
                }
            }
        }

        // Results Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.filteredTrails.size} Trekking Trails",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = uiState.filterState.sortOption.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { showFilterSheet = true }
                        .padding(4.dp)
                )
            }
        }

        if (uiState.filteredTrails.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Terrain,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No matching trails found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing your state/difficulty filters or sync live OpenStreetMap paths around your location.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = onClearFilters) {
                            Text("Reset Filters")
                        }
                    }
                }
            }
        } else {
            items(
                items = uiState.filteredTrails,
                key = { it.trail.id }
            ) { item ->
                TrailCard(
                    trail = item.trail,
                    imageUrl = uiState.trailImageUrls[item.trail.id],
                    distanceFromUserKm = item.distanceFromUserKm,
                    onClick = { onOpenTrailDetail(item.trail) },
                    onToggleSave = { onToggleSaveTrail(item.trail.id) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
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

@Composable
private fun RegionBannerCard(
    region: TrekRegion,
    imageUrl: String?,
    trailCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onMapClick: () -> Unit
) {
    val borderColor = if (isSelected) Terracotta else Color.Transparent
    Box(
        modifier = Modifier
            .width(210.dp)
            .height(122.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(2.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("region_card_${region.name}")
    ) {
        TrailPhotoImage(
            imageUrl = imageUrl,
            fallbackDrawable = region.bannerDrawable,
            contentDescription = region.displayName,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.15f),
                            ForestDeep.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(99.dp))
                .clickable(onClick = onMapClick),
            shape = RoundedCornerShape(99.dp),
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Explore,
                    contentDescription = "Open ${region.displayName} on map",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Map",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = region.displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "$trailCount trails • ${region.subtitle}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.86f),
                maxLines = 1
            )
        }
    }
}
