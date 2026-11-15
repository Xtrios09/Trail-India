package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GeoUtils
import com.example.domain.model.Trail
import com.example.ui.components.OsmMapComposable
import com.example.ui.components.rememberTrailHaptics
import com.example.ui.theme.ForestDeep
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PineGreen
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.RecordingStatus
import com.example.ui.viewmodel.TrailIndiaUiState
import kotlin.math.roundToInt

@Composable
fun RecordHikeScreen(
    uiState: TrailIndiaUiState,
    onRequestLocationPermission: () -> Unit,
    onStartRecording: (Trail?) -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onStopAndSave: (String?) -> Unit,
    onDiscardRecording: () -> Unit,
    onOpenOfflineLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recorder = uiState.recorderState
    val haptics = rememberTrailHaptics()
    val linkedTrail = uiState.allTrails.find {
        it.id == (recorder.linkedTrailId ?: uiState.followingTrailId)
    }

    var cameraLat by remember {
        mutableDoubleStateOf(uiState.liveGps.latitude ?: linkedTrail?.trailheadLat ?: 32.2425)
    }
    var cameraLon by remember {
        mutableDoubleStateOf(uiState.liveGps.longitude ?: linkedTrail?.trailheadLon ?: 76.3323)
    }
    var cameraZoom by remember { mutableDoubleStateOf(14.0) }
    var cameraKey by remember { mutableIntStateOf(0) }
    var pendingCenterOnGps by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.liveGps.latitude, uiState.liveGps.longitude, pendingCenterOnGps) {
        val lat = uiState.liveGps.latitude
        val lon = uiState.liveGps.longitude
        if (pendingCenterOnGps && lat != null && lon != null) {
            pendingCenterOnGps = false
            cameraLat = lat
            cameraLon = lon
            cameraZoom = 15.0
            cameraKey++
        }
    }

    var showSaveDialog by remember { mutableStateOf(false) }
    var customTitle by remember(recorder.linkedTrailName) {
        mutableStateOf(recorder.linkedTrailName?.let { "$it Trek" } ?: "India Trek Log")
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Upper Live OSM Map Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            OsmMapComposable(
                modifier = Modifier.fillMaxSize(),
                trails = linkedTrail?.let { listOf(it) } ?: emptyList(),
                selectedTrail = linkedTrail,
                followingTrail = linkedTrail,
                recordedPoints = recorder.trackPoints,
                osmPeaks = uiState.liveOsmPeaks,
                showPeakLabels = uiState.showPeakLabels,
                liveGps = uiState.liveGps,
                layerStyle = uiState.mapLayerStyle,
                isOfflineMapMode = uiState.isOfflineMapMode,
                cameraCenterLat = cameraLat,
                cameraCenterLon = cameraLon,
                cameraZoom = cameraZoom,
                cameraRequestKey = cameraKey,
                fitTrailBounds = false
            )

            // Top GPS Status Pill + History shortcut
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = ForestDeep.copy(alpha = 0.88f),
                    shadowElevation = 4.dp
                ) {
                    val gpsText = when {
                        !uiState.liveGps.hasLocationPermission -> "GPS permission needed"
                        uiState.liveGps.accuracyM != null -> "GPS Locked • ±${uiState.liveGps.accuracyM.roundToInt()} m"
                        else -> "Searching GPS satellites…"
                    }
                    Text(
                        text = gpsText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallFloatingActionButton(
                        onClick = {
                            haptics.tap()
                            onOpenOfflineLibrary()
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Rounded.History, contentDescription = "View recorded hike logs")
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            haptics.tap()
                            if (!uiState.liveGps.hasLocationPermission) {
                                pendingCenterOnGps = true
                                onRequestLocationPermission()
                            } else if (uiState.liveGps.latitude != null && uiState.liveGps.longitude != null) {
                                pendingCenterOnGps = false
                                cameraLat = uiState.liveGps.latitude
                                cameraLon = uiState.liveGps.longitude
                                cameraZoom = 15.0
                                cameraKey++
                            } else {
                                pendingCenterOnGps = true
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Rounded.MyLocation, contentDescription = "Center on my location")
                    }
                }
            }
        }

        // Bottom Deep Forest Green GPS Recorder HUD (Reference Image 5)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ForestDeep,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status + Linked Trail
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = when (recorder.status) {
                                        RecordingStatus.RECORDING -> Terracotta
                                        RecordingStatus.PAUSED -> Color(0xFFF59E0B)
                                        RecordingStatus.IDLE -> Color(0xFF74C69D)
                                    },
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = when (recorder.status) {
                                RecordingStatus.RECORDING -> "RECORDING"
                                RecordingStatus.PAUSED -> "PAUSED"
                                RecordingStatus.IDLE -> "GPS TRACKER READY"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    if (linkedTrail != null) {
                        Text(
                            text = linkedTrail.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF74C69D)
                        )
                    }
                }

                // Big Distance & Moving Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%.2f", recorder.distanceKm),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "km",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.78f),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "MOVING TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        Text(
                            text = GeoUtils.formatTimer(recorder.elapsedSeconds),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Terracotta
                        )
                    }
                }

                // 3-Column Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RecorderMetricItem(
                        value = "${recorder.ascentM} m",
                        label = "ASCENT",
                        modifier = Modifier.weight(1f)
                    )
                    val displayAlt = if (recorder.currentAltitudeM > 0) {
                        recorder.currentAltitudeM
                    } else {
                        uiState.liveGps.altitudeM?.roundToInt() ?: 0
                    }
                    RecorderMetricItem(
                        value = "$displayAlt m",
                        label = "ALTITUDE",
                        modifier = Modifier.weight(1f)
                    )
                    RecorderMetricItem(
                        value = GeoUtils.formatPace(recorder.avgPaceMinPerKm),
                        label = "AVG PACE",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Action Pill Buttons
                when (recorder.status) {
                    RecordingStatus.IDLE -> {
                        Button(
                            onClick = {
                                haptics.confirm()
                                if (!uiState.liveGps.hasLocationPermission) {
                                    onRequestLocationPermission()
                                } else {
                                    onStartRecording(linkedTrail)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("start_recording_button"),
                            shape = RoundedCornerShape(99.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.liveGps.hasLocationPermission) "Start GPS Hike" else "Enable GPS & Start",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                    RecordingStatus.RECORDING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptics.tap()
                                    onPauseRecording()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("pause_recording_button"),
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PineGreen)
                            ) {
                                Icon(Icons.Rounded.Pause, contentDescription = "Pause")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause")
                            }
                            Button(
                                onClick = {
                                    haptics.heavyAction()
                                    showSaveDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("stop_recording_button"),
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                            ) {
                                Icon(Icons.Rounded.Stop, contentDescription = "Stop")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Finish")
                            }
                        }
                    }
                    RecordingStatus.PAUSED -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptics.confirm()
                                    onResumeRecording()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PineGreen)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "Resume")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume")
                            }
                            Button(
                                onClick = {
                                    haptics.heavyAction()
                                    showSaveDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                            ) {
                                Icon(Icons.Rounded.Stop, contentDescription = "Finish")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Finish")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Hike Recording") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${String.format("%.2f", recorder.distanceKm)} km • ${GeoUtils.formatTimer(recorder.elapsedSeconds)} • +${recorder.ascentM} m",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily)
                    )
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text("Hike Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.confirm()
                        showSaveDialog = false
                        onStopAndSave(customTitle)
                    }
                ) {
                    Text("Save Locally")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            haptics.heavyAction()
                            showSaveDialog = false
                            onDiscardRecording()
                        }
                    ) {
                        Text("Discard", color = Terracotta)
                    }
                    OutlinedButton(onClick = {
                        haptics.tap()
                        showSaveDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
private fun RecorderMetricItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.65f)
        )
    }
}
