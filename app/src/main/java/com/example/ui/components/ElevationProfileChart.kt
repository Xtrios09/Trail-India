package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.GeoPointData
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TrailEmerald
import kotlin.math.roundToInt

@Composable
fun ElevationProfileChart(
    pathPoints: List<GeoPointData>,
    totalDistanceKm: Double,
    minElevationM: Int,
    maxElevationM: Int,
    elevationGainM: Int,
    modifier: Modifier = Modifier
) {
    val elevations = pathPoints.map { it.elevationM }
    val hasElevationData = elevations.size >= 2 && elevations.any { it > 0.0 } && maxElevationM > 0

    if (!hasElevationData) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Terrain,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Elevation Profile Unavailable",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "This live OpenStreetMap way does not include SRTM/ele tags. Distance (${String.format("%.1f", totalDistanceKm)} km) is verified from OSM geometry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    var scrubbedFraction by remember { mutableStateOf<Float?>(null) }

    val minVal = elevations.minOrNull() ?: minElevationM.toDouble()
    val maxVal = elevations.maxOrNull() ?: maxElevationM.toDouble()
    val span = (maxVal - minVal).coerceAtLeast(30.0)

    val activeIdx = scrubbedFraction?.let { frac ->
        (frac * (elevations.size - 1)).roundToInt().coerceIn(0, elevations.lastIndex)
    }
    val activeElev = activeIdx?.let { elevations[it].roundToInt() }
    val activeDistKm = scrubbedFraction?.let { it * totalDistanceKm }
    val activeGradePct = activeIdx?.let { idx ->
        if (idx > 0 && totalDistanceKm > 0) {
            val dElev = elevations[idx] - elevations[idx - 1]
            val segMeters = (totalDistanceKm * 1000.0) / (elevations.size - 1).coerceAtLeast(1)
            ((dElev / segMeters) * 100.0).roundToInt()
        } else 0
    }

    val lineColor = TrailEmerald
    val fillTop = TrailEmerald.copy(alpha = 0.35f)
    val fillBottom = TrailEmerald.copy(alpha = 0.03f)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ELEVATION PROFILE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (activeElev != null && activeDistKm != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${String.format("%.1f", activeDistKm)} km  •  $activeElev m  •  ${activeGradePct ?: 0}% grade",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                Text(
                    text = "Drag chart to inspect",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(elevations) {
                        detectTapGestures { offset ->
                            scrubbedFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        }
                    }
                    .pointerInput(elevations) {
                        detectDragGestures(
                            onDragEnd = { scrubbedFraction = null },
                            onDragCancel = { scrubbedFraction = null }
                        ) { change, _ ->
                            scrubbedFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Horizontal reference contour lines
                listOf(0.25f, 0.5f, 0.75f).forEach { yRatio ->
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, h * yRatio),
                        end = Offset(w, h * yRatio),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }

                val coords = elevations.mapIndexed { idx, elev ->
                    val x = (idx.toFloat() / (elevations.size - 1).coerceAtLeast(1)) * w
                    val normY = ((elev - minVal) / span).toFloat().coerceIn(0f, 1f)
                    val y = h - (normY * (h * 0.85f)) - (h * 0.08f)
                    Offset(x, y)
                }

                val linePath = Path().apply {
                    coords.forEachIndexed { idx, pt ->
                        if (idx == 0) moveTo(pt.x, pt.y)
                        else {
                            val prev = coords[idx - 1]
                            val midX = (prev.x + pt.x) / 2f
                            cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                        }
                    }
                }

                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(coords.last().x, h)
                    lineTo(coords.first().x, h)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(listOf(fillTop, fillBottom))
                )

                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Waypoint dots along curve
                coords.forEachIndexed { idx, pt ->
                    val dotColor = if (idx == coords.lastIndex) Terracotta else lineColor
                    drawCircle(color = Color.White, radius = 6.5f, center = pt)
                    drawCircle(color = dotColor, radius = 4f, center = pt)
                }

                // Scrubber vertical line & highlight
                scrubbedFraction?.let { frac ->
                    val sx = frac * w
                    val exactIdx = (frac * (coords.size - 1)).roundToInt().coerceIn(0, coords.lastIndex)
                    val target = coords[exactIdx]
                    drawLine(
                        color = Terracotta,
                        start = Offset(sx, 0f),
                        end = Offset(sx, h),
                        strokeWidth = 3f
                    )
                    drawCircle(color = Color.White, radius = 9f, center = target)
                    drawCircle(color = Terracotta, radius = 6f, center = target)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Min: $minElevationM m",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Gain: +$elevationGainM m",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Max: $maxElevationM m",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                color = Terracotta
            )
        }
    }
}
