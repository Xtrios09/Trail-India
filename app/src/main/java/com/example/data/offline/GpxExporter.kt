package com.example.data.offline

import android.content.Context
import android.content.Intent
import com.example.domain.model.GeoPointData
import com.example.domain.model.RecordedHike
import com.example.domain.model.Trail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GpxExporter {

    fun buildTrailGpx(trail: Trail): String {
        val trkPts = trail.pathPoints.joinToString("\n") { pt ->
            """      <trkpt lat="${pt.lat}" lon="${pt.lon}"><ele>${pt.elevationM}</ele></trkpt>"""
        }
        val wpts = trail.waypoints.joinToString("\n") { wp ->
            """  <wpt lat="${wp.lat}" lon="${wp.lon}"><ele>${wp.elevationM}</ele><name>${escapeXml(wp.name)}</name><desc>${escapeXml(wp.note)}</desc></wpt>"""
        }
        return """<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="TrailIndia Android" xmlns="http://www.topografix.com/GPX/1/1">
  <metadata>
    <name>${escapeXml(trail.name)}</name>
    <desc>${escapeXml("${trail.state} • ${trail.region.displayName} • ${trail.osmAttributionNote}")}</desc>
  </metadata>
$wpts
  <trk>
    <name>${escapeXml(trail.name)}</name>
    <trkseg>
$trkPts
    </trkseg>
  </trk>
</gpx>"""
    }

    fun buildRecordedHikeGpx(hike: RecordedHike): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val timestamp = isoFormat.format(Date(hike.startedAtEpochMs))
        val trkPts = hike.trackPoints.joinToString("\n") { pt: GeoPointData ->
            """      <trkpt lat="${pt.lat}" lon="${pt.lon}"><ele>${pt.elevationM}</ele></trkpt>"""
        }
        return """<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="TrailIndia GPS Recorder" xmlns="http://www.topografix.com/GPX/1/1">
  <metadata>
    <name>${escapeXml(hike.title)}</name>
    <time>$timestamp</time>
  </metadata>
  <trk>
    <name>${escapeXml(hike.title)}</name>
    <trkseg>
$trkPts
    </trkseg>
  </trk>
</gpx>"""
    }

    fun shareTrailSummaryAndGpx(context: Context, trail: Trail) {
        val gpxSnippet = buildTrailGpx(trail)
        val shareText = buildString {
            appendLine("${trail.name} (${trail.state}, ${trail.region.displayName})")
            appendLine("Distance: ${trail.distanceKm} km | Max Elevation: ${trail.maxElevationM} m | Gain: +${trail.elevationGainM} m")
            appendLine("Trailhead: ${trail.trailheadLat}, ${trail.trailheadLon}")
            appendLine("OpenStreetMap: https://www.openstreetmap.org/?mlat=${trail.trailheadLat}&mlon=${trail.trailheadLon}#map=14/${trail.trailheadLat}/${trail.trailheadLon}")
            appendLine()
            appendLine(gpxSnippet)
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "TrailIndia GPX: ${trail.name}")
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share ${trail.name} GPX"))
    }

    fun shareRecordedHike(context: Context, hike: RecordedHike) {
        val gpx = buildRecordedHikeGpx(hike)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "TrailIndia Hike Log: ${hike.title}")
            putExtra(Intent.EXTRA_TEXT, gpx)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Export ${hike.title} GPX"))
    }

    private fun escapeXml(input: String): String =
        input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
}
