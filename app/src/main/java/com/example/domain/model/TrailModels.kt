package com.example.domain.model

import androidx.annotation.DrawableRes
import com.example.R
import kotlinx.serialization.Serializable
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

enum class TrekRegion(
    val displayName: String,
    val subtitle: String,
    val centerLat: Double,
    val centerLon: Double,
    val defaultZoom: Double,
    @DrawableRes val bannerDrawable: Int
) {
    HIMALAYAS(
        displayName = "Himalayas",
        subtitle = "Uttarakhand • Himachal • Ladakh • Sikkim",
        centerLat = 31.1048,
        centerLon = 77.1734,
        defaultZoom = 6.5,
        bannerDrawable = R.drawable.img_region_himalayas_1794667468179
    ),
    NORTHEAST_INDIA(
        displayName = "Northeast India",
        subtitle = "Meghalaya • Nagaland • Singalila Ridge",
        centerLat = 26.2006,
        centerLon = 92.9376,
        defaultZoom = 7.0,
        bannerDrawable = R.drawable.img_region_northeast_1794667467881
    ),
    EASTERN_GHATS(
        displayName = "Eastern Ghats",
        subtitle = "Odisha • Andhra Pradesh • Tamil Nadu Hills",
        centerLat = 17.6868,
        centerLon = 82.4000,
        defaultZoom = 6.8,
        bannerDrawable = R.drawable.img_region_south_eastern_1794667468866
    ),
    WESTERN_GHATS(
        displayName = "Western Ghats",
        subtitle = "Sahyadris • Kodagu • Kudremukh",
        centerLat = 15.8497,
        centerLon = 74.4977,
        defaultZoom = 6.6,
        bannerDrawable = R.drawable.img_region_western_ghats_1794667469082
    ),
    SOUTH_INDIA(
        displayName = "South India",
        subtitle = "Nilgiris • Munnar • Wayanad • Velliangiri",
        centerLat = 10.8505,
        centerLon = 76.8500,
        defaultZoom = 7.5,
        bannerDrawable = R.drawable.img_region_south_eastern_1794667468866
    )
}

enum class Difficulty(val label: String) {
    EASY("Easy"),
    MODERATE("Moderate"),
    HARD("Hard"),
    CHALLENGING("Challenging")
}

enum class RouteType(val label: String) {
    OUT_AND_BACK("Out & back"),
    LOOP("Loop"),
    POINT_TO_POINT("Point-to-point")
}

enum class TrailDataSource(val label: String) {
    CURATED_ATLAS("Verified Atlas + OSM"),
    OSM_OVERPASS_LIVE("OpenStreetMap Live")
}

enum class MapLayerStyle(val label: String, val subtitle: String) {
    OPENTOPOMAP("OpenTopoMap", "Contours, peaks & terrain"),
    OSM_STANDARD("OSM Standard", "Classic street & path tiles"),
    CYCLOSM("CyclOSM Trail", "Footpaths, shelters & slopes")
}

enum class WaypointType(val label: String) {
    TRAILHEAD("Trailhead"),
    CAMPSITE("Campsite"),
    PASS("Mountain Pass"),
    PEAK("Summit / Peak"),
    WATER("Water Source"),
    VIEWPOINT("Viewpoint")
}

enum class TrailSortOption(val label: String) {
    FEATURED("Featured"),
    NEARBY("Nearest to Me"),
    SHORTEST("Shortest Distance"),
    HIGHEST_PEAK("Highest Elevation")
}

@Serializable
data class GeoPointData(
    val lat: Double,
    val lon: Double,
    val elevationM: Double
)

@Serializable
data class TrailWaypoint(
    val name: String,
    val type: WaypointType,
    val lat: Double,
    val lon: Double,
    val elevationM: Int,
    val distanceKm: Double,
    val note: String
)

data class Trail(
    val id: String,
    val name: String,
    val region: TrekRegion,
    val state: String,
    val area: String,
    val difficulty: Difficulty,
    val routeType: RouteType,
    val distanceKm: Double,
    val elevationGainM: Int,
    val maxElevationM: Int,
    val minElevationM: Int,
    val durationMinutes: Int,
    val bestSeason: String,
    val description: String,
    val highlights: List<String>,
    val trailheadName: String,
    val trailheadLat: Double,
    val trailheadLon: Double,
    val pathPoints: List<GeoPointData>,
    val waypoints: List<TrailWaypoint>,
    val dataSource: TrailDataSource,
    val osmAttributionNote: String,
    val isSaved: Boolean = false,
    val isOfflineDownloaded: Boolean = false,
    val offlineTileCount: Int = 0,
    val offlineSizeBytes: Long = 0L,
    val downloadedAtEpochMs: Long? = null
)

data class RecordedHike(
    val id: Long = 0,
    val title: String,
    val linkedTrailId: String? = null,
    val linkedTrailName: String? = null,
    val startedAtEpochMs: Long,
    val durationSeconds: Long,
    val distanceKm: Double,
    val ascentM: Int,
    val maxAltitudeM: Int,
    val avgPaceMinPerKm: Double,
    val trackPoints: List<GeoPointData>
)

data class LiveGpsState(
    val hasLocationPermission: Boolean = false,
    val isGpsEnabled: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitudeM: Double? = null,
    val accuracyM: Float? = null,
    val speedMps: Float? = null,
    val compassAzimuthDeg: Float = 0f,
    val lastFixEpochMs: Long? = null
)

data class OsmPeakMarker(
    val id: Long,
    val name: String,
    val lat: Double,
    val lon: Double,
    val elevationM: Int?
)

enum class WeatherConditionKind {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM
}

enum class WeatherAdvisoryLevel {
    GOOD,
    CAUTION,
    HAZARD
}

data class TrailWeather(
    val trailId: String,
    val trailName: String,
    val latitude: Double,
    val longitude: Double,
    val temperatureC: Double,
    val apparentTempC: Double,
    val humidityPct: Int,
    val windSpeedKmh: Double,
    val precipitationMm: Double,
    val cloudCoverPct: Int,
    val weatherCode: Int,
    val conditionKind: WeatherConditionKind,
    val conditionLabel: String,
    val trailAdvisory: String,
    val advisoryLevel: WeatherAdvisoryLevel,
    val isDay: Boolean,
    val elevationM: Int?,
    val observedAtEpochMs: Long,
    val isFromCache: Boolean = false
)

sealed interface TrailWeatherUiState {
    data object Idle : TrailWeatherUiState
    data class Loading(val trailId: String, val trailName: String) : TrailWeatherUiState
    data class Ready(val weather: TrailWeather) : TrailWeatherUiState
    data class Unavailable(
        val trailId: String,
        val trailName: String,
        val message: String,
        val isOffline: Boolean = false
    ) : TrailWeatherUiState
}

@Serializable
data class GeoBounds(
    val northLat: Double,
    val southLat: Double,
    val eastLon: Double,
    val westLon: Double
) {
    val centerLat: Double get() = (northLat + southLat) / 2.0
    val centerLon: Double get() = (eastLon + westLon) / 2.0
}

enum class OfflineZoomPreset(
    val label: String,
    val subtitle: String,
    val minZoom: Int,
    val maxZoom: Int
) {
    OVERVIEW("Overview", "Zoom 10–13", 10, 13),
    TRAIL("Trail", "Zoom 11–14", 11, 14),
    DETAILED("Detailed", "Zoom 12–15", 12, 15)
}

@Serializable
data class OfflineMapRegion(
    val id: String,
    val name: String,
    val bounds: GeoBounds,
    val minZoom: Int,
    val maxZoom: Int,
    val layerStyleName: String,
    val tileCount: Int,
    val sizeBytes: Long,
    val downloadedAtEpochMs: Long
)

object GeoUtils {
    private const val EARTH_RADIUS_KM = 6371.0

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    fun formatDuration(minutes: Int): String {
        return if (minutes >= 24 * 60) {
            val days = (minutes / (8.0 * 60.0)).coerceAtLeast(1.0)
            val roundedDays = days.toInt()
            if (roundedDays <= 1) "${minutes / 60}h ${minutes % 60}m"
            else "$roundedDays days (${minutes / 60}h)"
        } else {
            val h = minutes / 60
            val m = minutes % 60
            when {
                h > 0 && m > 0 -> "${h}h ${m}m"
                h > 0 -> "${h}h"
                else -> "${m}m"
            }
        }
    }

    fun formatTimer(totalSeconds: Long): String {
        val hrs = totalSeconds / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return String.format("%d:%02d:%02d", hrs, mins, secs)
    }

    fun formatPace(minPerKm: Double): String {
        if (minPerKm <= 0.0 || minPerKm.isInfinite() || minPerKm.isNaN()) return "--:--"
        val capped = minPerKm.coerceAtMost(99.9)
        val mins = capped.toInt()
        val secs = ((capped - mins) * 60).toInt().coerceIn(0, 59)
        return String.format("%d:%02d", mins, secs)
    }
}
