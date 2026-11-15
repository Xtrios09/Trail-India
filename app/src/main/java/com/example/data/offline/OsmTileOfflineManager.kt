package com.example.data.offline

import android.content.Context
import com.example.domain.model.GeoBounds
import com.example.domain.model.GeoPointData
import com.example.domain.model.MapLayerStyle
import com.example.domain.model.Trail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.modules.SqlTileWriter
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

data class OfflineDownloadProgress(
    val trailId: String,
    val downloadedTiles: Int,
    val totalTiles: Int
) {
    val fraction: Float
        get() = if (totalTiles <= 0) 0f else (downloadedTiles.toFloat() / totalTiles.toFloat()).coerceIn(0f, 1f)
}

data class OfflineDownloadResult(
    val tileCount: Int,
    val totalBytes: Long
)

data class RegionTileEstimate(
    val rawTileCount: Int,
    val cappedTileCount: Int,
    val estimatedBytes: Long,
    val exceedsCap: Boolean
)

object OsmTileOfflineManager {

    const val MAX_MANUAL_REGION_TILES = 400
    private const val ESTIMATED_BYTES_PER_TILE = 16_000L

    val openTopoMapSource: XYTileSource = XYTileSource(
        "OpenTopoMap",
        1,
        17,
        256,
        ".png",
        arrayOf(
            "https://a.tile.opentopomap.org/",
            "https://b.tile.opentopomap.org/",
            "https://c.tile.opentopomap.org/"
        ),
        "© OpenStreetMap contributors, SRTM | Map style: © OpenTopoMap (CC-BY-SA)"
    )

    val cycloOsmSource: XYTileSource = XYTileSource(
        "CyclOSM",
        1,
        17,
        256,
        ".png",
        arrayOf(
            "https://a.tile-cyclosm.openstreetmap.fr/cyclosm/",
            "https://b.tile-cyclosm.openstreetmap.fr/cyclosm/",
            "https://c.tile-cyclosm.openstreetmap.fr/cyclosm/"
        ),
        "© OpenStreetMap contributors | CyclOSM"
    )

    fun tileSourceFor(style: MapLayerStyle): ITileSource = when (style) {
        MapLayerStyle.OPENTOPOMAP -> openTopoMapSource
        MapLayerStyle.OSM_STANDARD -> TileSourceFactory.MAPNIK
        MapLayerStyle.CYCLOSM -> cycloOsmSource
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Volatile
    private var configured = false

    fun ensureOsmdroidConfigured(context: Context) {
        if (configured) return
        synchronized(this) {
            if (configured) return
            val appCtx = context.applicationContext
            val baseDir = File(appCtx.filesDir, "osmdroid").apply { mkdirs() }
            val tileCacheDir = File(baseDir, "tiles").apply { mkdirs() }

            val config = Configuration.getInstance()
            config.userAgentValue = appCtx.packageName
            config.osmdroidBasePath = baseDir
            config.osmdroidTileCache = tileCacheDir
            config.tileDownloadThreads = 4
            config.tileFileSystemThreads = 4
            config.expirationOverrideDuration = 1000L * 60L * 60L * 24L * 30L // 30 days offline retention
            configured = true
        }
    }

    fun getCacheSizeBytes(context: Context): Long {
        val baseDir = File(context.applicationContext.filesDir, "osmdroid")
        if (!baseDir.exists()) return 0L
        return baseDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun clearTileCache(context: Context): Boolean {
        val baseDir = File(context.applicationContext.filesDir, "osmdroid")
        val deleted = if (baseDir.exists()) baseDir.deleteRecursively() else true
        File(baseDir, "tiles").mkdirs()
        return deleted
    }

    fun estimateRegionTiles(
        bounds: GeoBounds,
        minZoom: Int,
        maxZoom: Int,
        maxTiles: Int = MAX_MANUAL_REGION_TILES
    ): RegionTileEstimate {
        val north = max(bounds.northLat, bounds.southLat)
        val south = min(bounds.northLat, bounds.southLat)
        val east = max(bounds.eastLon, bounds.westLon)
        val west = min(bounds.eastLon, bounds.westLon)

        var rawCount = 0
        for (z in minZoom..maxZoom) {
            val (x1, y1) = latLonToTileXY(north, west, z)
            val (x2, y2) = latLonToTileXY(south, east, z)
            val startX = min(x1, x2)
            val endX = max(x1, x2)
            val startY = min(y1, y2)
            val endY = max(y1, y2)
            rawCount += (endX - startX + 1) * (endY - startY + 1)
        }
        val capped = rawCount.coerceAtMost(maxTiles).coerceAtLeast(1)
        return RegionTileEstimate(
            rawTileCount = rawCount,
            cappedTileCount = capped,
            estimatedBytes = capped * ESTIMATED_BYTES_PER_TILE,
            exceedsCap = rawCount > maxTiles
        )
    }

    fun computeBoundingBoxTiles(
        bounds: GeoBounds,
        minZoom: Int,
        maxZoom: Int,
        maxTiles: Int = MAX_MANUAL_REGION_TILES
    ): List<Triple<Int, Int, Int>> {
        val north = max(bounds.northLat, bounds.southLat)
        val south = min(bounds.northLat, bounds.southLat)
        val east = max(bounds.eastLon, bounds.westLon)
        val west = min(bounds.eastLon, bounds.westLon)

        val result = LinkedHashSet<Triple<Int, Int, Int>>()
        for (z in minZoom..maxZoom) {
            val (x1, y1) = latLonToTileXY(north, west, z)
            val (x2, y2) = latLonToTileXY(south, east, z)
            val startX = min(x1, x2)
            val endX = max(x1, x2)
            val startY = min(y1, y2)
            val endY = max(y1, y2)
            for (x in startX..endX) {
                for (y in startY..endY) {
                    result.add(Triple(z, x, y))
                    if (result.size >= maxTiles) {
                        return result.toList()
                    }
                }
            }
        }
        return result.toList()
    }

    suspend fun downloadBoundingBoxTiles(
        context: Context,
        regionId: String,
        bounds: GeoBounds,
        minZoom: Int,
        maxZoom: Int,
        layerStyle: MapLayerStyle,
        onProgress: (OfflineDownloadProgress) -> Unit
    ): Result<OfflineDownloadResult> = withContext(Dispatchers.IO) {
        runCatching {
            ensureOsmdroidConfigured(context)
            val tiles = computeBoundingBoxTiles(
                bounds = bounds,
                minZoom = minZoom,
                maxZoom = maxZoom,
                maxTiles = MAX_MANUAL_REGION_TILES
            )
            downloadTilesInternal(
                context = context,
                progressId = regionId,
                tiles = tiles,
                layerStyle = layerStyle,
                onProgress = onProgress
            )
        }
    }

    suspend fun downloadTrailTiles(
        context: Context,
        trail: Trail,
        layerStyle: MapLayerStyle,
        onProgress: (OfflineDownloadProgress) -> Unit
    ): Result<OfflineDownloadResult> = withContext(Dispatchers.IO) {
        runCatching {
            ensureOsmdroidConfigured(context)
            val points = trail.pathPoints.ifEmpty {
                listOf(GeoPointData(trail.trailheadLat, trail.trailheadLon, trail.minElevationM.toDouble()))
            }

            val tiles = computeCorridorTiles(points, minZoom = 11, maxZoom = 14)
                .take(180) // Bounded polite batch so downloads complete fast & respect free OSM tile servers

            downloadTilesInternal(
                context = context,
                progressId = trail.id,
                tiles = tiles,
                layerStyle = layerStyle,
                onProgress = onProgress
            )
        }
    }

    private suspend fun downloadTilesInternal(
        context: Context,
        progressId: String,
        tiles: List<Triple<Int, Int, Int>>,
        layerStyle: MapLayerStyle,
        onProgress: (OfflineDownloadProgress) -> Unit
    ): OfflineDownloadResult {
        val tileSource = tileSourceFor(layerStyle)
        val writer = SqlTileWriter()
        val completed = AtomicInteger(0)
        val totalBytes = AtomicLong(0L)
        val total = tiles.size.coerceAtLeast(1)
        onProgress(OfflineDownloadProgress(progressId, 0, total))

        try {
            tiles.chunked(4).forEach { chunk ->
                coroutineScope {
                    chunk.map { (z, x, y) ->
                        async {
                            val tileIndex = MapTileIndex.getTileIndex(z, x, y)
                            val alreadyCached = runCatching { writer.exists(tileSource, tileIndex) }.getOrDefault(false)
                            if (alreadyCached) {
                                totalBytes.addAndGet(14_000L)
                            } else {
                                val url = tileUrlFor(layerStyle, z, x, y)
                                val request = Request.Builder()
                                    .url(url)
                                    .header("User-Agent", "${context.packageName} TrailIndia/1.0")
                                    .build()
                                runCatching {
                                    httpClient.newCall(request).execute().use { resp ->
                                        if (resp.isSuccessful) {
                                            val bytes = resp.body?.bytes()
                                            if (bytes != null && bytes.isNotEmpty()) {
                                                writer.saveFile(
                                                    tileSource,
                                                    tileIndex,
                                                    ByteArrayInputStream(bytes),
                                                    System.currentTimeMillis() + 1000L * 60L * 60L * 24L * 60L
                                                )
                                                totalBytes.addAndGet(bytes.size.toLong())
                                            }
                                        }
                                    }
                                }
                            }
                            val done = completed.incrementAndGet()
                            onProgress(OfflineDownloadProgress(progressId, done, total))
                        }
                    }.awaitAll()
                }
            }
        } finally {
            runCatching { writer.onDetach() }
        }

        return OfflineDownloadResult(
            tileCount = tiles.size,
            totalBytes = totalBytes.get().coerceAtLeast(tiles.size * 12_000L)
        )
    }

    private fun tileUrlFor(style: MapLayerStyle, z: Int, x: Int, y: Int): String {
        val server = listOf("a", "b", "c")[(x + y) % 3]
        return when (style) {
            MapLayerStyle.OPENTOPOMAP -> "https://$server.tile.opentopomap.org/$z/$x/$y.png"
            MapLayerStyle.OSM_STANDARD -> "https://tile.openstreetmap.org/$z/$x/$y.png"
            MapLayerStyle.CYCLOSM -> "https://$server.tile-cyclosm.openstreetmap.fr/cyclosm/$z/$x/$y.png"
        }
    }

    private fun computeCorridorTiles(
        points: List<GeoPointData>,
        minZoom: Int,
        maxZoom: Int
    ): List<Triple<Int, Int, Int>> {
        val paddingDeg = 0.018 // ~2 km buffer around trail
        val minLat = points.minOf { it.lat } - paddingDeg
        val maxLat = points.maxOf { it.lat } + paddingDeg
        val minLon = points.minOf { it.lon } - paddingDeg
        val maxLon = points.maxOf { it.lon } + paddingDeg

        val result = LinkedHashSet<Triple<Int, Int, Int>>()
        for (z in minZoom..maxZoom) {
            val (x1, y1) = latLonToTileXY(maxLat, minLon, z)
            val (x2, y2) = latLonToTileXY(minLat, maxLon, z)
            val startX = min(x1, x2)
            val endX = max(x1, x2)
            val startY = min(y1, y2)
            val endY = max(y1, y2)

            // Guard huge multi-day treks at high zoom by focusing around path points
            val tileSpan = (endX - startX + 1) * (endY - startY + 1)
            if (tileSpan <= 64) {
                for (x in startX..endX) {
                    for (y in startY..endY) {
                        result.add(Triple(z, x, y))
                    }
                }
            } else {
                points.forEach { pt ->
                    val (cx, cy) = latLonToTileXY(pt.lat, pt.lon, z)
                    for (dx in -1..1) {
                        for (dy in -1..1) {
                            result.add(Triple(z, cx + dx, cy + dy))
                        }
                    }
                }
            }
        }
        return result.toList()
    }

    private fun latLonToTileXY(lat: Double, lon: Double, zoom: Int): Pair<Int, Int> {
        val n = 1 shl zoom
        val clampedLat = lat.coerceIn(-85.0511, 85.0511)
        val latRad = Math.toRadians(clampedLat)
        val x = (((lon + 180.0) / 360.0) * n).toInt().coerceIn(0, n - 1)
        val y = ((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n)
            .toInt()
            .coerceIn(0, n - 1)
        return x to y
    }
}
