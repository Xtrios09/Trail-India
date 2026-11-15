package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.offline.GpxExporter
import com.example.data.offline.OfflineDownloadProgress
import com.example.domain.model.GeoUtils
import com.example.domain.model.Trail
import com.example.domain.model.WaypointType
import com.example.ui.components.DifficultyChip
import com.example.ui.components.ElevationProfileChart
import com.example.ui.components.TrailPhotoImage
import com.example.ui.components.rememberTrailHaptics
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TrailEmerald

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrailDetailScreen(
    trail: Trail,
    imageUrl: String? = null,
    downloadProgress: OfflineDownloadProgress?,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    onDownloadOffline: () -> Unit,
    onRemoveOffline: () -> Unit,
    onNavigateOnMap: () -> Unit,
    onStartRecordingOnTrail: () -> Unit
) {
    val context = LocalContext.current
    val haptics = rememberTrailHaptics()

    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 10.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (downloadProgress != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Caching OSM tiles offline…",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "${downloadProgress.downloadedTiles}/${downloadProgress.totalTiles}",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress.fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (trail.isOfflineDownloaded) {
                            OutlinedButton(
                                onClick = {
                                    haptics.heavyAction()
                                    onRemoveOffline()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("offline_ready_button")
                            ) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = TrailEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val mb = String.format("%.1f", trail.offlineSizeBytes / (1024.0 * 1024.0))
                                Text("Offline ($mb MB)")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Rounded.DeleteOutline,
                                    contentDescription = "Remove offline bundle",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    haptics.confirm()
                                    onDownloadOffline()
                                },
                                enabled = downloadProgress == null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("download_offline_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = TrailEmerald)
                            ) {
                                if (downloadProgress != null) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download")
                            }
                        }

                        Button(
                            onClick = {
                                haptics.confirm()
                                onNavigateOnMap()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("follow_trail_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                Icons.Rounded.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Map & GPS")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                TrailPhotoImage(
                    imageUrl = imageUrl,
                    fallbackDrawable = trail.region.bannerDrawable,
                    contentDescription = trail.name,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.5f)) {
                        IconButton(
                            onClick = {
                                haptics.tap()
                                onBack()
                            },
                            modifier = Modifier.testTag("detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.5f)) {
                            IconButton(
                                onClick = {
                                    haptics.tap()
                                    GpxExporter.shareTrailSummaryAndGpx(context, trail)
                                },
                                modifier = Modifier.testTag("share_gpx_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = "Share GPX",
                                    tint = Color.White
                                )
                            }
                        }
                        Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.5f)) {
                            IconButton(
                                onClick = {
                                    haptics.confirm()
                                    onToggleSave()
                                },
                                modifier = Modifier.testTag("detail_save_button")
                            ) {
                                Icon(
                                    imageVector = if (trail.isSaved) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Save trail",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Content Sheet
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DifficultyChip(difficulty = trail.difficulty)
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = trail.dataSource.label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = trail.name,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "${trail.region.displayName} • ${trail.state} • ${trail.area}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 2x2 Big Bold Stats Grid (Reference Image 3)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatGridCell(
                                label = "Distance",
                                value = "${trail.distanceKm} km",
                                icon = Icons.Rounded.Straighten,
                                modifier = Modifier.weight(1f)
                            )
                            StatGridCell(
                                label = "Elevation Gain",
                                value = if (trail.elevationGainM > 0) "+${trail.elevationGainM} m" else "N/A",
                                icon = Icons.Rounded.Landscape,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatGridCell(
                                label = "Est. Duration",
                                value = GeoUtils.formatDuration(trail.durationMinutes),
                                icon = Icons.Rounded.Schedule,
                                modifier = Modifier.weight(1f)
                            )
                            StatGridCell(
                                label = "Route Type",
                                value = trail.routeType.label,
                                icon = Icons.Rounded.Route,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Key Metadata Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (trail.maxElevationM > 0) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Max Alt: ${trail.maxElevationM} m") }
                        )
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text("Best Season: ${trail.bestSeason}") }
                    )
                    AssistChip(
                        onClick = {
                            haptics.confirm()
                            onStartRecordingOnTrail()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Explore,
                                contentDescription = null,
                                tint = Terracotta,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Record This Hike") }
                    )
                }

                // Trail Description
                Text(
                    text = trail.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                // Interactive Elevation Profile
                ElevationProfileChart(
                    pathPoints = trail.pathPoints,
                    totalDistanceKm = trail.distanceKm,
                    minElevationM = trail.minElevationM,
                    maxElevationM = trail.maxElevationM,
                    elevationGainM = trail.elevationGainM
                )

                // Waypoints / Timeline
                if (trail.waypoints.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "ROUTE WAYPOINTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        trail.waypoints.forEach { wp ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Icon(
                                            imageVector = when (wp.type) {
                                                WaypointType.WATER -> Icons.Rounded.WaterDrop
                                                WaypointType.PEAK, WaypointType.PASS -> Icons.Rounded.Landscape
                                                else -> Icons.Rounded.LocationOn
                                            },
                                            contentDescription = wp.type.label,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(18.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = wp.name,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = wp.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${wp.distanceKm} km",
                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily)
                                        )
                                        if (wp.elevationM > 0) {
                                            Text(
                                                text = "${wp.elevationM} m",
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                                color = Terracotta
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Trailhead Coordinates & OSM Attribution
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Trailhead: ${trail.trailheadName}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "GPS: ${String.format("%.4f", trail.trailheadLat)}° N, ${String.format("%.4f", trail.trailheadLon)}° E",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (!imageUrl.isNullOrBlank()) {
                                "${trail.osmAttributionNote} Trail photo via Wikimedia Commons / Wikipedia."
                            } else {
                                trail.osmAttributionNote
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatGridCell(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold
            )
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
