package com.example.data.remote

import com.example.domain.model.Difficulty
import com.example.domain.model.GeoPointData
import com.example.domain.model.GeoUtils
import com.example.domain.model.OsmPeakMarker
import com.example.domain.model.RouteType
import com.example.domain.model.Trail
import com.example.domain.model.TrailDataSource
import com.example.domain.model.TrailWaypoint
import com.example.domain.model.TrekRegion
import com.example.domain.model.WaypointType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class OverpassQueryResult(
    val trails: List<Trail>,
    val peaks: List<OsmPeakMarker>
)

class OverpassOsmClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    suspend fun fetchNearbyTrailsAndPeaks(
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Int = 25000
    ): Result<OverpassQueryResult> = withContext(Dispatchers.IO) {
        runCatching {
            val query = """
                [out:json][timeout:20];
                (
                  way["highway"~"path|footway|track"]["name"](around:$radiusMeters,$centerLat,$centerLon);
                  relation["route"="hiking"]["name"](around:$radiusMeters,$centerLat,$centerLon);
                  node["natural"="peak"]["name"](around:${radiusMeters * 2},$centerLat,$centerLon);
                );
                out geom 80;
            """.trimIndent()

            val body = FormBody.Builder()
                .add("data", query)
                .build()

            val request = Request.Builder()
                .url("https://overpass-api.de/api/interpreter")
                .header("User-Agent", "TrailIndia-Android/1.0 (OpenStreetMap Offline Trekking App)")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("Overpass API returned HTTP ${response.code}")
                }
                val payload = response.body?.string().orEmpty()
                if (payload.isBlank()) error("Empty response from Overpass API")
                parseOverpassJson(payload)
            }
        }
    }

    private fun parseOverpassJson(rawJson: String): OverpassQueryResult {
        val root = JSONObject(rawJson)
        val elements = root.optJSONArray("elements") ?: return OverpassQueryResult(emptyList(), emptyList())

        val trails = mutableListOf<Trail>()
        val peaks = mutableListOf<OsmPeakMarker>()

        for (i in 0 until elements.length()) {
            val el = elements.optJSONObject(i) ?: continue
            val type = el.optString("type")
            val id = el.optLong("id")
            val tags = el.optJSONObject("tags") ?: continue
            val name = tags.optString("name").trim()
            if (name.isBlank()) continue

            if (type == "node" && tags.optString("natural") == "peak") {
                val lat = el.optDouble("lat", Double.NaN)
                val lon = el.optDouble("lon", Double.NaN)
                if (!lat.isNaN() && !lon.isNaN()) {
                    val eleRaw = tags.optString("ele").replace("m", "", ignoreCase = true).trim()
                    val ele = eleRaw.toDoubleOrNull()?.roundToInt()
                    peaks.add(OsmPeakMarker(id = id, name = name, lat = lat, lon = lon, elevationM = ele))
                }
            } else if (type == "way") {
                val geom = el.optJSONArray("geometry") ?: continue
                if (geom.length() < 3) continue

                val points = mutableListOf<GeoPointData>()
                for (j in 0 until geom.length()) {
                    val pt = geom.optJSONObject(j) ?: continue
                    val lat = pt.optDouble("lat", Double.NaN)
                    val lon = pt.optDouble("lon", Double.NaN)
                    if (!lat.isNaN() && !lon.isNaN()) {
                        points.add(GeoPointData(lat = lat, lon = lon, elevationM = 0.0))
                    }
                }
                if (points.size < 3) continue

                var totalKm = 0.0
                for (k in 1 until points.size) {
                    totalKm += GeoUtils.haversineKm(
                        points[k - 1].lat, points[k - 1].lon,
                        points[k].lat, points[k].lon
                    )
                }
                if (totalKm < 0.4) continue

                val start = points.first()
                val end = points.last()
                val region = inferIndianRegion(start.lat, start.lon)
                val sacScale = tags.optString("sac_scale")
                val difficulty = when {
                    sacScale.contains("alpine") || totalKm > 15.0 -> Difficulty.HARD
                    sacScale.contains("mountain") || totalKm > 7.0 -> Difficulty.MODERATE
                    else -> Difficulty.EASY
                }
                val surface = tags.optString("surface").takeIf { it.isNotBlank() } ?: "natural trail"
                val estMinutes = ((totalKm / 3.2) * 60).roundToInt().coerceAtLeast(25)

                trails.add(
                    Trail(
                        id = "osm_way_$id",
                        name = name,
                        region = region,
                        state = inferApproxStateLabel(start.lat, start.lon),
                        area = "OpenStreetMap Way #$id",
                        difficulty = difficulty,
                        routeType = if (GeoUtils.haversineKm(start.lat, start.lon, end.lat, end.lon) < 0.25) {
                            RouteType.LOOP
                        } else {
                            RouteType.POINT_TO_POINT
                        },
                        distanceKm = (totalKm * 10.0).roundToInt() / 10.0,
                        elevationGainM = 0,
                        maxElevationM = 0,
                        minElevationM = 0,
                        durationMinutes = estMinutes,
                        bestSeason = "Check local weather",
                        description = "Live OpenStreetMap trail geometry ($surface) imported directly from OSM way $id. Elevation metadata is omitted when not tagged in OSM.",
                        highlights = listOf(
                            "Live OSM Way #$id",
                            "Surface: $surface",
                            "${points.size} mapped nodes"
                        ),
                        trailheadName = "$name Start",
                        trailheadLat = start.lat,
                        trailheadLon = start.lon,
                        pathPoints = points,
                        waypoints = listOf(
                            TrailWaypoint("OSM Start Node", WaypointType.TRAILHEAD, start.lat, start.lon, 0, 0.0, "Mapped OSM start node"),
                            TrailWaypoint("OSM End Node", WaypointType.VIEWPOINT, end.lat, end.lon, 0, (totalKm * 10.0).roundToInt() / 10.0, "Mapped OSM end node")
                        ),
                        dataSource = TrailDataSource.OSM_OVERPASS_LIVE,
                        osmAttributionNote = "Live geometry © OpenStreetMap contributors (way/$id)."
                    )
                )
            }
        }
        return OverpassQueryResult(
            trails = trails.distinctBy { it.name.lowercase() }.take(25),
            peaks = peaks.distinctBy { it.id }.take(40)
        )
    }

    private fun inferIndianRegion(lat: Double, lon: Double): TrekRegion {
        return when {
            lat >= 27.0 && lon <= 89.0 -> TrekRegion.HIMALAYAS
            lon >= 88.0 && lat >= 22.0 -> TrekRegion.NORTHEAST_INDIA
            lon >= 79.0 && lat in 11.5..22.5 -> TrekRegion.EASTERN_GHATS
            lat <= 12.2 -> TrekRegion.SOUTH_INDIA
            else -> TrekRegion.WESTERN_GHATS
        }
    }

    private fun inferApproxStateLabel(lat: Double, lon: Double): String {
        return when {
            lat >= 32.5 -> "Ladakh / J&K"
            lat >= 30.8 && lon < 77.9 -> "Himachal Pradesh"
            lat >= 29.5 && lon in 77.9..81.0 -> "Uttarakhand"
            lat in 26.5..28.3 && lon in 87.8..89.0 -> "Sikkim / Darjeeling"
            lon >= 90.0 && lat >= 24.5 -> "Northeast India"
            lat in 17.0..21.5 && lon >= 81.0 -> "Odisha / Andhra Pradesh"
            lat in 15.0..20.8 && lon <= 76.5 -> "Maharashtra / Goa"
            lat in 12.0..15.5 && lon <= 77.5 -> "Karnataka"
            lat < 12.0 && lon < 76.6 -> "Kerala"
            else -> "Tamil Nadu"
        }
    }
}
