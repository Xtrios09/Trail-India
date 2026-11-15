package com.example

import com.example.data.catalog.VerifiedIndianTrailsCatalog
import com.example.data.offline.GpxExporter
import com.example.data.offline.OsmTileOfflineManager
import com.example.data.remote.OpenMeteoWeatherClient
import com.example.domain.model.GeoBounds
import com.example.domain.model.GeoUtils
import com.example.domain.model.TrekRegion
import com.example.domain.model.WeatherAdvisoryLevel
import com.example.domain.model.WeatherConditionKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailIndiaCoreTest {

    @Test
    fun catalogCoversAllFiveIndianRegionsWithValidCoordinates() {
        val trails = VerifiedIndianTrailsCatalog.allTrails
        assertTrue(trails.size >= 15)

        TrekRegion.entries.forEach { region ->
            val regionTrails = trails.filter { it.region == region }
            assertTrue("Missing trails for ${region.displayName}", regionTrails.isNotEmpty())
        }

        trails.forEach { trail ->
            assertTrue("${trail.name} latitude out of India range", trail.trailheadLat in 6.0..37.5)
            assertTrue("${trail.name} longitude out of India range", trail.trailheadLon in 68.0..97.5)
            assertTrue("${trail.name} must have positive distance", trail.distanceKm > 0.0)
            assertTrue("${trail.name} must have >= 2 path points", trail.pathPoints.size >= 2)
            assertTrue("${trail.name} max elevation must exceed min", trail.maxElevationM >= trail.minElevationM)
        }
    }

    @Test
    fun geoUtilsCalculationsAndFormattingWork() {
        // Approximate distance from McLeod Ganj/Galu Devi to Triund top (~2.5-3 km crow flies)
        val crowKm = GeoUtils.haversineKm(32.2425, 76.3323, 32.2608, 76.3490)
        assertTrue(crowKm in 2.0..3.5)

        assertEquals("4h 30m", GeoUtils.formatDuration(270))
        assertEquals("1:05:09", GeoUtils.formatTimer(3909))
        assertEquals("12:30", GeoUtils.formatPace(12.5))
        assertEquals("--:--", GeoUtils.formatPace(0.0))
    }

    @Test
    fun gpxExporterProducesValidGpxXml() {
        val trail = VerifiedIndianTrailsCatalog.allTrails.first()
        val gpx = GpxExporter.buildTrailGpx(trail)
        assertTrue(gpx.contains("<gpx version=\"1.1\""))
        assertTrue(gpx.contains("<trkpt lat=\"${trail.pathPoints.first().lat}\""))
        assertTrue(gpx.contains("<wpt lat=\"${trail.waypoints.first().lat}\""))
    }

    @Test
    fun openMeteoWmoMappingAndAdvisoriesWork() {
        assertEquals(WeatherConditionKind.CLEAR to "Clear sky", OpenMeteoWeatherClient.mapWmoCode(0))
        assertEquals(WeatherConditionKind.SNOW to "Snowfall", OpenMeteoWeatherClient.mapWmoCode(73))
        assertEquals(WeatherConditionKind.THUNDERSTORM to "Thunderstorm", OpenMeteoWeatherClient.mapWmoCode(95))

        val (_, stormLevel) = OpenMeteoWeatherClient.buildTrailAdvisory(
            kind = WeatherConditionKind.THUNDERSTORM,
            tempC = 10.0,
            windKmh = 25.0,
            precipMm = 4.0
        )
        assertEquals(WeatherAdvisoryLevel.HAZARD, stormLevel)

        val (_, goodLevel) = OpenMeteoWeatherClient.buildTrailAdvisory(
            kind = WeatherConditionKind.PARTLY_CLOUDY,
            tempC = 16.0,
            windKmh = 12.0,
            precipMm = 0.0
        )
        assertEquals(WeatherAdvisoryLevel.GOOD, goodLevel)
    }

    @Test
    fun manualRegionTileEstimationAndBoundingBoxRespectCaps() {
        val triundArea = GeoBounds(
            northLat = 32.275,
            southLat = 32.225,
            eastLon = 76.365,
            westLon = 76.305
        )
        val estimate = OsmTileOfflineManager.estimateRegionTiles(
            bounds = triundArea,
            minZoom = 11,
            maxZoom = 14
        )
        assertTrue(estimate.rawTileCount > 0)
        assertTrue(estimate.cappedTileCount <= OsmTileOfflineManager.MAX_MANUAL_REGION_TILES)
        assertTrue(estimate.estimatedBytes > 0L)

        val tiles = OsmTileOfflineManager.computeBoundingBoxTiles(
            bounds = triundArea,
            minZoom = 11,
            maxZoom = 14
        )
        assertTrue(tiles.isNotEmpty())
        assertTrue(tiles.all { (z, _, _) -> z in 11..14 })

        val hugeArea = GeoBounds(
            northLat = 34.0,
            southLat = 30.0,
            eastLon = 80.0,
            westLon = 75.0
        )
        val hugeEstimate = OsmTileOfflineManager.estimateRegionTiles(
            bounds = hugeArea,
            minZoom = 10,
            maxZoom = 15
        )
        assertTrue(hugeEstimate.exceedsCap)
        assertEquals(OsmTileOfflineManager.MAX_MANUAL_REGION_TILES, hugeEstimate.cappedTileCount)
        assertEquals(
            OsmTileOfflineManager.MAX_MANUAL_REGION_TILES,
            OsmTileOfflineManager.computeBoundingBoxTiles(hugeArea, 10, 15).size
        )
    }
}
