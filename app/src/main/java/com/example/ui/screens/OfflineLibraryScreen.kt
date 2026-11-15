package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Hiking
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.offline.GpxExporter
import com.example.domain.model.GeoUtils
import com.example.domain.model.OfflineMapRegion
import com.example.domain.model.RecordedHike
import com.example.domain.model.Trail
import com.example.ui.components.rememberTrailHaptics
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TrailEmerald
import com.example.ui.viewmodel.TrailIndiaUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OfflineLibraryScreen(
    uiState: TrailIndiaUiState,
    onToggleOfflineMapMode: (Boolean) -> Unit,
    onClearTileCache: () -> Unit,
    onOpenTrailDetail: (Trail) -> Unit,
    onDownloadTrailOffline: (Trail) -> Unit,
    onRemoveOfflineTrail: (String) -> Unit,
    onToggleSaveTrail: (String) -> Unit,
    onDeleteRecordedHike: (Long) -> Unit,
    onOpenRegionSelector: () -> Unit = {},
    onOpenOfflineRegionOnMap: (OfflineMapRegion) -> Unit = {},
    onRemoveOfflineRegion: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberTrailHaptics()
    val offlineTrails = uiState.allTrails.filter { it.isOfflineDownloaded }
    val savedOnlyTrails = uiState.allTrails.filter { it.isSaved && !it.isOfflineDownloaded }
    val cacheMb = String.format("%.1f", uiState.totalTileCacheBytes / (1024.0 * 1024.0))
    var easterEggExpanded by remember { mutableStateOf(false) }
    var headerTapCount by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    headerTapCount++
                    if (headerTapCount >= 3) {
                        easterEggExpanded = true
                    }
                }
            ) {
                Text(
                    text = "Offline & Saved",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "OpenStreetMap tile packs, saved routes & GPS hike logs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Offline Mode + OSM Tile Storage Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudOff,
                                contentDescription = null,
                                tint = if (uiState.isOfflineMapMode) Terracotta else MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Offline Map Mode",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Serve cached OSM tiles only (saves battery & works without signal)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.isOfflineMapMode,
                            onCheckedChange = { enabled ->
                                haptics.tap()
                                onToggleOfflineMapMode(enabled)
                            },
                            modifier = Modifier.testTag("offline_mode_switch")
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "OSM Tile Cache",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "$cacheMb MB • ${offlineTrails.size} trails • ${uiState.offlineRegions.size} areas",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            OutlinedButton(onClick = {
                                haptics.heavyAction()
                                onClearTileCache()
                            }) {
                                Text("Clear")
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            haptics.tap()
                            onOpenRegionSelector()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("pick_offline_region_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CropFree,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Custom Map Area to Cache")
                    }
                }
            }
        }

        // Custom Offline Map Areas
        item {
            Text(
                text = "CUSTOM OFFLINE MAP AREAS (${uiState.offlineRegions.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.offlineRegions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CropFree,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tap Select Custom Map Area to frame any valley, ridge, or town and cache OpenStreetMap tiles for offline trekking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.offlineRegions, key = { "region_${it.id}" }) { region ->
                val mb = String.format("%.1f", region.sizeBytes / (1024.0 * 1024.0))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptics.tap()
                            onOpenOfflineRegionOnMap(region)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Map,
                                contentDescription = null,
                                tint = TrailEmerald
                            )
                            Column {
                                Text(region.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "Z${region.minZoom}–${region.maxZoom} • ${region.tileCount} tiles • $mb MB • ${region.layerStyleName}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                haptics.heavyAction()
                                onRemoveOfflineRegion(region.id)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Remove saved area"
                            )
                        }
                    }
                }
            }
        }

        // Offline Downloaded Trails
        item {
            Text(
                text = "DOWNLOADED OFFLINE TRAILS (${offlineTrails.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (offlineTrails.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Open any trail and tap Download to cache its OpenStreetMap tiles before heading into the mountains.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(offlineTrails, key = { "offline_${it.id}" }) { trail ->
                val mb = String.format("%.1f", trail.offlineSizeBytes / (1024.0 * 1024.0))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTrailDetail(trail) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudDone,
                                contentDescription = null,
                                tint = TrailEmerald
                            )
                            Column {
                                Text(trail.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${trail.state} • ${trail.offlineTileCount} tiles • $mb MB",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = {
                            haptics.heavyAction()
                            onRemoveOfflineTrail(trail.id)
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Remove offline bundle"
                            )
                        }
                    }
                }
            }
        }

        // Saved Bookmarks
        item {
            Text(
                text = "SAVED BOOKMARKS (${savedOnlyTrails.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (savedOnlyTrails.isEmpty()) {
            item {
                Text(
                    text = "No extra bookmarked trails. Tap the bookmark icon on any trail to save it here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(savedOnlyTrails, key = { "saved_${it.id}" }) { trail ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTrailDetail(trail) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(trail.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${trail.state} • ${trail.distanceKm} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    haptics.confirm()
                                    onDownloadTrailOffline(trail)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cache")
                            }
                            IconButton(onClick = {
                                haptics.tap()
                                onToggleSaveTrail(trail.id)
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Bookmark,
                                    contentDescription = "Unsave",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recorded GPS Hikes
        item {
            Text(
                text = "RECORDED GPS HIKES (${uiState.recordedHikes.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.recordedHikes.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Hiking,
                            contentDescription = null,
                            tint = Terracotta
                        )
                        Text(
                            text = "Use the Record tab to log your trek via satellite GPS even with zero cellular coverage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.recordedHikes, key = { "hike_${it.id}" }) { hike ->
                RecordedHikeCard(
                    hike = hike,
                    onShareGpx = { GpxExporter.shareRecordedHike(context, hike) },
                    onDelete = { onDeleteRecordedHike(hike.id) }
                )
            }
        }

        // Subtle Easter Egg Footer
        item {
            val scale by animateFloatAsState(
                targetValue = if (easterEggExpanded) 1.05f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "easterEggScale"
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp)
                    .scale(scale)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptics.confirm()
                        easterEggExpanded = !easterEggExpanded
                    }
                    .testTag("offline_easter_egg"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = if (easterEggExpanded) Icons.Rounded.Favorite else Icons.Rounded.Terrain,
                        contentDescription = null,
                        tint = if (easterEggExpanded) Terracotta else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "made with love by abhijeet <3",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 0.4.sp
                        ),
                        color = if (easterEggExpanded) {
                            Terracotta
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        },
                        textAlign = TextAlign.Center
                    )
                }

                AnimatedVisibility(
                    visible = easterEggExpanded,
                    enter = fadeIn() + scaleIn(initialScale = 0.92f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = Terracotta.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Keep exploring the Himalayas & Ghats — stay on trail!",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Terracotta
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordedHikeCard(
    hike: RecordedHike,
    onShareGpx: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
        .format(Date(hike.startedAtEpochMs))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(hike.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onShareGpx) {
                        Icon(Icons.Rounded.Share, contentDescription = "Export GPX")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete hike")
                    }
                }
            }
            Text(
                text = "${String.format("%.2f", hike.distanceKm)} km  •  ${GeoUtils.formatTimer(hike.durationSeconds)}  •  +${hike.ascentM} m ascent",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
