package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.offline.OfflineDownloadProgress
import com.example.data.offline.OsmTileOfflineManager
import com.example.domain.model.GeoBounds
import com.example.domain.model.MapLayerStyle
import com.example.domain.model.OfflineZoomPreset
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MeadowMint
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TrailEmerald
import com.example.ui.theme.WarmAmber

enum class SelectorFrameSize(
    val label: String,
    val horizontalFraction: Float,
    val verticalFraction: Float
) {
    COMPACT("Small", 0.56f, 0.22f),
    MEDIUM("Medium", 0.76f, 0.30f),
    LARGE("Large", 0.88f, 0.36f)
}

@Composable
fun RegionSelectorOverlay(
    selectionBounds: GeoBounds?,
    frameSize: SelectorFrameSize,
    zoomPreset: OfflineZoomPreset,
    layerStyle: MapLayerStyle,
    downloadProgress: OfflineDownloadProgress?,
    suggestedRegionName: String?,
    onFrameSizeSelected: (SelectorFrameSize) -> Unit,
    onZoomPresetSelected: (OfflineZoomPreset) -> Unit,
    onDownloadClick: (regionName: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    centerBiasY: Float = 0.40f
) {
    val haptics = rememberTrailHaptics()
    var regionName by remember { mutableStateOf("") }
    var userEditedName by remember { mutableStateOf(false) }

    LaunchedEffect(selectionBounds, suggestedRegionName) {
        if (!userEditedName && selectionBounds != null) {
            regionName = buildDefaultRegionName(suggestedRegionName, selectionBounds)
        }
    }

    val estimate = remember(selectionBounds, zoomPreset) {
        selectionBounds?.let {
            OsmTileOfflineManager.estimateRegionTiles(
                bounds = it,
                minZoom = zoomPreset.minZoom,
                maxZoom = zoomPreset.maxZoom
            )
        }
    }

    val bracketColor = MeadowMint
    val frameStrokeColor = Color.White.copy(alpha = 0.88f)
    val scrimColor = Color.Black.copy(alpha = 0.42f)

    Box(modifier = modifier.fillMaxSize()) {
        // Non-touch-blocking viewfinder scrim & corner brackets around exact selected bounds
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val left = ((1f - frameSize.horizontalFraction) / 2f) * w
            val right = ((1f + frameSize.horizontalFraction) / 2f) * w
            val top = ((centerBiasY - frameSize.verticalFraction / 2f).coerceIn(0.05f, 0.90f)) * h
            val bottom = ((centerBiasY + frameSize.verticalFraction / 2f).coerceIn(0.10f, 0.95f)) * h
            val boxWidth = (right - left).coerceAtLeast(1f)
            val boxHeight = (bottom - top).coerceAtLeast(1f)

            // Darkened outer scrim strips (4 rectangles leaving the viewfinder transparent)
            drawRect(color = scrimColor, topLeft = Offset.Zero, size = Size(w, top))
            drawRect(color = scrimColor, topLeft = Offset(0f, bottom), size = Size(w, (h - bottom).coerceAtLeast(0f)))
            drawRect(color = scrimColor, topLeft = Offset(0f, top), size = Size(left, boxHeight))
            drawRect(color = scrimColor, topLeft = Offset(right, top), size = Size((w - right).coerceAtLeast(0f), boxHeight))

            // Subtle white frame border
            drawRect(
                color = frameStrokeColor,
                topLeft = Offset(left, top),
                size = Size(boxWidth, boxHeight),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Corner L-brackets
            val cornerLen = 22.dp.toPx()
            val strokeW = 4.dp.toPx()
            // Top-left
            drawLine(bracketColor, Offset(left, top), Offset(left + cornerLen, top), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(left, top), Offset(left, top + cornerLen), strokeW, StrokeCap.Round)
            // Top-right
            drawLine(bracketColor, Offset(right - cornerLen, top), Offset(right, top), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(right, top), Offset(right, top + cornerLen), strokeW, StrokeCap.Round)
            // Bottom-left
            drawLine(bracketColor, Offset(left, bottom - cornerLen), Offset(left, bottom), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeW, StrokeCap.Round)
            // Bottom-right
            drawLine(bracketColor, Offset(right - cornerLen, bottom), Offset(right, bottom), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(right, bottom - cornerLen), Offset(right, bottom), strokeW, StrokeCap.Round)

            // Center reticle dot
            val cx = (left + right) / 2f
            val cy = (top + bottom) / 2f
            drawCircle(
                color = bracketColor.copy(alpha = 0.9f),
                radius = 4.dp.toPx(),
                center = Offset(cx, cy)
            )
        }

        // Top Guidance Header
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CropFree,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Select Offline Map Area",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pan and pinch-zoom to frame your trekking area",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = {
                        haptics.tap()
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("close_region_selector_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Exit region selector"
                    )
                }
            }
        }

        // Bottom Region Config & Download Panel
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("region_selector_bottom_panel"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = regionName,
                    onValueChange = {
                        userEditedName = true
                        regionName = it
                    },
                    label = { Text("Area Name") },
                    placeholder = { Text("e.g. Dhauladhar Ridge") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("region_name_input")
                )

                // Frame Size + Zoom Detail Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Frame Size & Map Detail",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SelectorFrameSize.entries.forEach { sizeOption ->
                            val selected = sizeOption == frameSize
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    haptics.tap()
                                    onFrameSizeSelected(sizeOption)
                                },
                                label = { Text(sizeOption.label) },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OfflineZoomPreset.entries.forEach { preset ->
                            val selected = preset == zoomPreset
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    haptics.tap()
                                    onZoomPresetSelected(preset)
                                },
                                label = { Text("${preset.label} (${preset.minZoom}–${preset.maxZoom})") },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            )
                        }
                    }
                }

                // Live Tile & Size Readout
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (estimate != null) {
                            val estMb = String.format("%.1f", estimate.estimatedBytes / (1024.0 * 1024.0))
                            Text(
                                text = "${estimate.cappedTileCount} OSM tiles • ~$estMb MB • ${layerStyle.label}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = TrailEmerald
                            )
                            if (estimate.exceedsCap) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.WarningAmber,
                                        contentDescription = null,
                                        tint = WarmAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Capped at ${OsmTileOfflineManager.MAX_MANUAL_REGION_TILES} tiles (needs ${estimate.rawTileCount}). Zoom in or pick Small for full zoom.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WarmAmber
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Calculating map area bounds…",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (downloadProgress != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { downloadProgress.fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = Terracotta
                        )
                        Text(
                            text = "Caching ${downloadProgress.downloadedTiles} / ${downloadProgress.totalTiles} ${layerStyle.label} tiles…",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptics.tap()
                            onDismiss()
                        },
                        enabled = downloadProgress == null,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            haptics.confirm()
                            onDownloadClick(regionName)
                        },
                        enabled = selectionBounds != null && downloadProgress == null,
                        modifier = Modifier
                            .weight(1.4f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("download_region_tiles_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (downloadProgress != null) "Caching…" else "Download Area")
                    }
                }
            }
        }
    }
}

private fun buildDefaultRegionName(suggested: String?, bounds: GeoBounds): String {
    val latDir = if (bounds.centerLat >= 0) "N" else "S"
    val lonDir = if (bounds.centerLon >= 0) "E" else "W"
    val coords = String.format(
        "%.2f°%s, %.2f°%s",
        kotlin.math.abs(bounds.centerLat),
        latDir,
        kotlin.math.abs(bounds.centerLon),
        lonDir
    )
    return if (!suggested.isNullOrBlank()) {
        "$suggested ($coords)"
    } else {
        "Sector $coords"
    }
}
