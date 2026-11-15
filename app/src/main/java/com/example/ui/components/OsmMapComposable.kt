package com.example.ui.components

import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.offline.OsmTileOfflineManager
import com.example.domain.model.GeoBounds
import com.example.domain.model.GeoPointData
import com.example.domain.model.LiveGpsState
import com.example.domain.model.MapLayerStyle
import com.example.domain.model.OfflineMapRegion
import com.example.domain.model.OsmPeakMarker
import com.example.domain.model.Trail
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.ScaleBarOverlay

@Composable
fun OsmMapComposable(
    modifier: Modifier = Modifier,
    trails: List<Trail>,
    selectedTrail: Trail?,
    followingTrail: Trail?,
    recordedPoints: List<GeoPointData>,
    osmPeaks: List<OsmPeakMarker>,
    showPeakLabels: Boolean,
    liveGps: LiveGpsState,
    layerStyle: MapLayerStyle,
    isOfflineMapMode: Boolean,
    cameraCenterLat: Double,
    cameraCenterLon: Double,
    cameraZoom: Double,
    cameraRequestKey: Int,
    fitTrailBounds: Boolean = false,
    cachedRegions: List<OfflineMapRegion> = emptyList(),
    isRegionSelectMode: Boolean = false,
    selectionBoxHorizontalFraction: Float = 0.78f,
    selectionBoxVerticalFraction: Float = 0.32f,
    selectionCenterBiasY: Float = 0.40f,
    onSelectionBoundsChanged: (GeoBounds) -> Unit = {},
    onTrailSelected: (Trail) -> Unit = {},
    onMapCenterChanged: (Double, Double) -> Unit = { _, _ -> }
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var isReleased by remember { mutableStateOf(false) }
    var lastAppliedRequestKey by remember { mutableStateOf(-1) }
    var lastAppliedLayer by remember { mutableStateOf<MapLayerStyle?>(null) }

    val latestRegionMode by rememberUpdatedState(isRegionSelectMode)
    val latestHFrac by rememberUpdatedState(selectionBoxHorizontalFraction)
    val latestVFrac by rememberUpdatedState(selectionBoxVerticalFraction)
    val latestBiasY by rememberUpdatedState(selectionCenterBiasY)
    val latestOnSelectionBoundsChanged by rememberUpdatedState(onSelectionBoundsChanged)
    val latestOnMapCenterChanged by rememberUpdatedState(onMapCenterChanged)

    LaunchedEffect(
        isRegionSelectMode,
        selectionBoxHorizontalFraction,
        selectionBoxVerticalFraction,
        selectionCenterBiasY,
        cameraRequestKey,
        mapViewRef
    ) {
        val mv = mapViewRef ?: return@LaunchedEffect
        if (isRegionSelectMode && !isReleased) {
            mv.post {
                if (!isReleased) {
                    computeViewfinderBounds(mv, latestHFrac, latestVFrac, latestBiasY)?.let {
                        latestOnSelectionBoundsChanged(it)
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val mv = mapViewRef ?: return@LifecycleEventObserver
            if (isReleased) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_RESUME -> mv.onResume()
                Lifecycle.Event.ON_PAUSE -> mv.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            OsmTileOfflineManager.ensureOsmdroidConfigured(ctx)
            isReleased = false
            MapView(ctx).apply {
                setDestroyMode(false)
                setMultiTouchControls(true)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                minZoomLevel = 4.0
                maxZoomLevel = 17.5
                isTilesScaledToDpi = true
                setTileSource(OsmTileOfflineManager.tileSourceFor(layerStyle))
                setUseDataConnection(!isOfflineMapMode)
                controller.setZoom(cameraZoom)
                controller.setCenter(GeoPoint(cameraCenterLat, cameraCenterLon))
                val initialFocusTrail = followingTrail ?: selectedTrail
                if (fitTrailBounds && initialFocusTrail != null && initialFocusTrail.pathPoints.size >= 2) {
                    val box = BoundingBox.fromGeoPoints(
                        initialFocusTrail.pathPoints.map { GeoPoint(it.lat, it.lon) }
                    )
                    post {
                        if (!isReleased) {
                            runCatching { zoomToBoundingBox(box, false, 90) }
                        }
                    }
                }
                addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        if (!isReleased) {
                            latestOnMapCenterChanged(mapCenter.latitude, mapCenter.longitude)
                            if (latestRegionMode) {
                                computeViewfinderBounds(this@apply, latestHFrac, latestVFrac, latestBiasY)?.let {
                                    latestOnSelectionBoundsChanged(it)
                                }
                            }
                        }
                        return false
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        if (!isReleased) {
                            latestOnMapCenterChanged(mapCenter.latitude, mapCenter.longitude)
                            if (latestRegionMode) {
                                computeViewfinderBounds(this@apply, latestHFrac, latestVFrac, latestBiasY)?.let {
                                    latestOnSelectionBoundsChanged(it)
                                }
                            }
                        }
                        return false
                    }
                })
                lastAppliedLayer = layerStyle
                lastAppliedRequestKey = cameraRequestKey
                mapViewRef = this
            }
        },
        onRelease = { mv ->
            isReleased = true
            mv.onPause()
            mv.onDetach()
            if (mapViewRef === mv) {
                mapViewRef = null
            }
        },
        update = { mapView ->
            if (isReleased) return@AndroidView
            mapView.setUseDataConnection(!isOfflineMapMode)
            if (lastAppliedLayer != layerStyle) {
                mapView.setTileSource(OsmTileOfflineManager.tileSourceFor(layerStyle))
                lastAppliedLayer = layerStyle
            }

            if (lastAppliedRequestKey != cameraRequestKey) {
                lastAppliedRequestKey = cameraRequestKey
                mapView.controller.stopAnimation(false)
                val focusTrail = followingTrail ?: selectedTrail
                if (fitTrailBounds && focusTrail != null && focusTrail.pathPoints.size >= 2) {
                    val box = BoundingBox.fromGeoPoints(
                        focusTrail.pathPoints.map { GeoPoint(it.lat, it.lon) }
                    )
                    mapView.post {
                        if (!isReleased) {
                            runCatching {
                                mapView.zoomToBoundingBox(box, true, 90)
                            }.onFailure {
                                mapView.controller.animateTo(
                                    GeoPoint(cameraCenterLat, cameraCenterLon),
                                    cameraZoom,
                                    550L
                                )
                            }
                        }
                    }
                } else {
                    mapView.controller.animateTo(
                        GeoPoint(cameraCenterLat, cameraCenterLon),
                        cameraZoom,
                        550L
                    )
                }
            }

            // Rebuild overlays cleanly for current state
            mapView.overlays.clear()

            mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    p?.let { onMapCenterChanged(mapView.mapCenter.latitude, mapView.mapCenter.longitude) }
                    return false
                }

                override fun longPressHelper(p: GeoPoint?): Boolean = false
            }))

            if (cachedRegions.isNotEmpty()) {
                mapView.overlays.add(OfflineRegionsOverlay(cachedRegions))
            }

            // Draw all trails' simplified lines faintly when zoomed out
            trails.forEach { trail ->
                val isHighlighted = trail.id == selectedTrail?.id || trail.id == followingTrail?.id
                if (!isHighlighted && trail.pathPoints.size >= 2) {
                    val line = Polyline().apply {
                        setPoints(trail.pathPoints.map { GeoPoint(it.lat, it.lon) })
                        outlinePaint.color = AndroidColor.argb(130, 45, 106, 79)
                        outlinePaint.strokeWidth = 6f
                        outlinePaint.isAntiAlias = true
                    }
                    mapView.overlays.add(line)
                }
            }

            // Draw active/selected trail polyline prominently
            val primaryTrail = followingTrail ?: selectedTrail
            if (primaryTrail != null && primaryTrail.pathPoints.size >= 2) {
                val trailPolyline = Polyline().apply {
                    setPoints(primaryTrail.pathPoints.map { GeoPoint(it.lat, it.lon) })
                    outlinePaint.color = if (followingTrail != null) {
                        AndroidColor.parseColor("#E05A2B")
                    } else {
                        AndroidColor.parseColor("#1B4332")
                    }
                    outlinePaint.strokeWidth = 11f
                    outlinePaint.isAntiAlias = true
                    if (primaryTrail.routeType.name == "LOOP") {
                        outlinePaint.pathEffect = DashPathEffect(floatArrayOf(24f, 10f), 0f)
                    }
                }
                mapView.overlays.add(trailPolyline)

                // Waypoint dots along selected trail
                primaryTrail.waypoints.forEach { wp ->
                    val wpMarker = Marker(mapView).apply {
                        position = GeoPoint(wp.lat, wp.lon)
                        title = wp.name
                        snippet = if (wp.elevationM > 0) "${wp.elevationM} m • ${wp.note}" else wp.note
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = createCircleDrawable(
                            mapView.context,
                            fillColor = AndroidColor.parseColor("#D48C29"),
                            strokeColor = AndroidColor.WHITE,
                            sizeDp = 14
                        )
                    }
                    mapView.overlays.add(wpMarker)
                }
            }

            // Draw trailhead markers for all filtered trails
            trails.forEach { trail ->
                val isSelected = trail.id == primaryTrail?.id
                val marker = Marker(mapView).apply {
                    position = GeoPoint(trail.trailheadLat, trail.trailheadLon)
                    title = trail.name
                    snippet = "${trail.state} • ${trail.distanceKm} km"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    icon = createCircleDrawable(
                        mapView.context,
                        fillColor = when {
                            isSelected -> AndroidColor.parseColor("#E05A2B")
                            trail.isOfflineDownloaded -> AndroidColor.parseColor("#40916C")
                            else -> AndroidColor.parseColor("#1B4332")
                        },
                        strokeColor = AndroidColor.WHITE,
                        sizeDp = if (isSelected) 20 else 14
                    )
                    setOnMarkerClickListener { _, _ ->
                        onTrailSelected(trail)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }

            // Draw nearby OSM peaks
            if (showPeakLabels && osmPeaks.isNotEmpty()) {
                osmPeaks.forEach { peak ->
                    val peakMarker = Marker(mapView).apply {
                        position = GeoPoint(peak.lat, peak.lon)
                        title = if (peak.elevationM != null) "${peak.name} (${peak.elevationM} m)" else peak.name
                        snippet = "OpenStreetMap Peak"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = createCircleDrawable(
                            mapView.context,
                            fillColor = AndroidColor.parseColor("#1D4ED8"),
                            strokeColor = AndroidColor.WHITE,
                            sizeDp = 12
                        )
                    }
                    mapView.overlays.add(peakMarker)
                }
            }

            // Draw recorded GPS hike polyline
            if (recordedPoints.size >= 2) {
                val recLine = Polyline().apply {
                    setPoints(recordedPoints.map { GeoPoint(it.lat, it.lon) })
                    outlinePaint.color = AndroidColor.parseColor("#E05A2B")
                    outlinePaint.strokeWidth = 12f
                    outlinePaint.isAntiAlias = true
                }
                mapView.overlays.add(recLine)
            }

            // Draw user's live GPS location dot
            if (liveGps.latitude != null && liveGps.longitude != null) {
                val userGeo = GeoPoint(liveGps.latitude, liveGps.longitude)
                mapView.overlays.add(UserGpsDotOverlay(userGeo))
            }

            // Add scale bar
            val scaleBar = ScaleBarOverlay(mapView).apply {
                setAlignBottom(false)
                setAlignRight(false)
                setScaleBarOffset(28, 28)
            }
            mapView.overlays.add(scaleBar)

            mapView.invalidate()
        }
    )
}

private fun computeViewfinderBounds(
    mapView: MapView,
    hFrac: Float,
    vFrac: Float,
    centerBiasY: Float
): GeoBounds? {
    if (mapView.width <= 0 || mapView.height <= 0) return null
    val leftPx = (((1f - hFrac) / 2f) * mapView.width).toInt()
    val rightPx = (((1f + hFrac) / 2f) * mapView.width).toInt()
    val topPx = ((centerBiasY - vFrac / 2f).coerceIn(0.05f, 0.90f) * mapView.height).toInt()
    val bottomPx = ((centerBiasY + vFrac / 2f).coerceIn(0.10f, 0.95f) * mapView.height).toInt()
    val topLeft = mapView.projection.fromPixels(leftPx, topPx) ?: return null
    val bottomRight = mapView.projection.fromPixels(rightPx, bottomPx) ?: return null
    val north = maxOf(topLeft.latitude, bottomRight.latitude)
    val south = minOf(topLeft.latitude, bottomRight.latitude)
    val east = maxOf(topLeft.longitude, bottomRight.longitude)
    val west = minOf(topLeft.longitude, bottomRight.longitude)
    if (north <= south || east <= west) return null
    return GeoBounds(
        northLat = north,
        southLat = south,
        eastLon = east,
        westLon = west
    )
}

private class OfflineRegionsOverlay(
    private val regions: List<OfflineMapRegion>
) : Overlay() {
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(32, 64, 145, 108)
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(200, 27, 67, 50)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(16f, 10f), 0f)
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val proj = mapView.projection
        regions.forEach { region ->
            val topLeft = proj.toPixels(GeoPoint(region.bounds.northLat, region.bounds.westLon), null)
            val bottomRight = proj.toPixels(GeoPoint(region.bounds.southLat, region.bounds.eastLon), null)
            val rect = RectF(
                minOf(topLeft.x, bottomRight.x).toFloat(),
                minOf(topLeft.y, bottomRight.y).toFloat(),
                maxOf(topLeft.x, bottomRight.x).toFloat(),
                maxOf(topLeft.y, bottomRight.y).toFloat()
            )
            if (rect.width() >= 6f && rect.height() >= 6f) {
                canvas.drawRoundRect(rect, 12f, 12f, fillPaint)
                canvas.drawRoundRect(rect, 12f, 12f, strokePaint)
            }
        }
    }
}

private class UserGpsDotOverlay(private val point: GeoPoint) : Overlay() {
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(65, 37, 99, 235)
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }
    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#2563EB")
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val screenPt = mapView.projection.toPixels(point, null)
        canvas.drawCircle(screenPt.x.toFloat(), screenPt.y.toFloat(), 32f, haloPaint)
        canvas.drawCircle(screenPt.x.toFloat(), screenPt.y.toFloat(), 17f, borderPaint)
        canvas.drawCircle(screenPt.x.toFloat(), screenPt.y.toFloat(), 12f, corePaint)
    }
}

private fun createCircleDrawable(
    context: android.content.Context,
    fillColor: Int,
    strokeColor: Int,
    sizeDp: Int
): GradientDrawable {
    val px = (sizeDp * context.resources.displayMetrics.density).toInt().coerceAtLeast(16)
    val strokePx = (2.5f * context.resources.displayMetrics.density).toInt().coerceAtLeast(2)
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setSize(px, px)
        setColor(fillColor)
        setStroke(strokePx, strokeColor)
    }
}
