package com.example.data.remote

import com.example.domain.model.Trail
import com.example.domain.model.TrailWeather
import com.example.domain.model.WeatherAdvisoryLevel
import com.example.domain.model.WeatherConditionKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class OpenMeteoWeatherClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val cache = ConcurrentHashMap<String, TrailWeather>()

    fun getCachedWeather(trailId: String, maxAgeMs: Long = CACHE_TTL_MS): TrailWeather? {
        val entry = cache[trailId] ?: return null
        val age = System.currentTimeMillis() - entry.observedAtEpochMs
        return if (age <= maxAgeMs) entry else null
    }

    suspend fun fetchCurrentTrailWeather(
        trail: Trail,
        forceRefresh: Boolean = false
    ): Result<TrailWeather> = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            getCachedWeather(trail.id, CACHE_TTL_MS)?.let { cached ->
                return@withContext Result.success(cached)
            }
        }

        val networkResult = runCatching {
            val url = "https://api.open-meteo.com/v1/forecast".toHttpUrl()
                .newBuilder()
                .addQueryParameter("latitude", trail.trailheadLat.toString())
                .addQueryParameter("longitude", trail.trailheadLon.toString())
                .addQueryParameter(
                    "current",
                    "temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,weather_code,cloud_cover,wind_speed_10m,is_day"
                )
                .addQueryParameter("timezone", "auto")
                .build()

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "TrailIndia-Android/1.0 (Open-Source Trekking App)")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("Weather API returned HTTP ${response.code}")
                }
                val payload = response.body?.string().orEmpty()
                if (payload.isBlank()) error("Empty response from Open-Meteo")
                parseOpenMeteoJson(
                    rawJson = payload,
                    trailId = trail.id,
                    trailName = trail.name,
                    fallbackLat = trail.trailheadLat,
                    fallbackLon = trail.trailheadLon
                )
            }
        }

        networkResult.onSuccess { fresh ->
            cache[trail.id] = fresh
        }

        if (networkResult.isFailure) {
            val stale = cache[trail.id]
            if (stale != null) {
                return@withContext Result.success(stale.copy(isFromCache = true))
            }
        }

        networkResult
    }

    companion object {
        const val CACHE_TTL_MS = 15 * 60 * 1000L

        fun parseOpenMeteoJson(
            rawJson: String,
            trailId: String,
            trailName: String,
            fallbackLat: Double,
            fallbackLon: Double,
            observedAtEpochMs: Long = System.currentTimeMillis()
        ): TrailWeather {
            val root = JSONObject(rawJson)
            val current = root.optJSONObject("current")
                ?: error("Missing current weather block in Open-Meteo response")

            val lat = root.optDouble("latitude", fallbackLat)
            val lon = root.optDouble("longitude", fallbackLon)
            val elevationRaw = root.optDouble("elevation", Double.NaN)
            val elevationM = if (!elevationRaw.isNaN()) elevationRaw.roundToInt() else null

            val tempC = current.optDouble("temperature_2m", Double.NaN)
            if (tempC.isNaN()) error("Missing temperature_2m in weather response")
            val apparentC = current.optDouble("apparent_temperature", tempC)
                .let { if (it.isNaN()) tempC else it }
            val humidity = current.optDouble("relative_humidity_2m", 0.0)
                .let { if (it.isNaN()) 0 else it.roundToInt().coerceIn(0, 100) }
            val precip = current.optDouble("precipitation", 0.0)
                .let { if (it.isNaN()) 0.0 else it.coerceAtLeast(0.0) }
            val cloudCover = current.optDouble("cloud_cover", 0.0)
                .let { if (it.isNaN()) 0 else it.roundToInt().coerceIn(0, 100) }
            val windKmh = current.optDouble("wind_speed_10m", 0.0)
                .let { if (it.isNaN()) 0.0 else it.coerceAtLeast(0.0) }
            val weatherCode = current.optInt("weather_code", 0)
            val isDay = current.optInt("is_day", 1) == 1

            val (kind, label) = mapWmoCode(weatherCode)
            val (advisory, level) = buildTrailAdvisory(
                kind = kind,
                tempC = tempC,
                windKmh = windKmh,
                precipMm = precip
            )

            return TrailWeather(
                trailId = trailId,
                trailName = trailName,
                latitude = lat,
                longitude = lon,
                temperatureC = tempC,
                apparentTempC = apparentC,
                humidityPct = humidity,
                windSpeedKmh = windKmh,
                precipitationMm = precip,
                cloudCoverPct = cloudCover,
                weatherCode = weatherCode,
                conditionKind = kind,
                conditionLabel = label,
                trailAdvisory = advisory,
                advisoryLevel = level,
                isDay = isDay,
                elevationM = elevationM,
                observedAtEpochMs = observedAtEpochMs,
                isFromCache = false
            )
        }

        fun mapWmoCode(code: Int): Pair<WeatherConditionKind, String> = when (code) {
            0 -> WeatherConditionKind.CLEAR to "Clear sky"
            1 -> WeatherConditionKind.PARTLY_CLOUDY to "Mainly clear"
            2 -> WeatherConditionKind.PARTLY_CLOUDY to "Partly cloudy"
            3 -> WeatherConditionKind.CLOUDY to "Overcast"
            45, 48 -> WeatherConditionKind.FOG to "Mountain fog"
            51, 53, 55, 56, 57 -> WeatherConditionKind.DRIZZLE to "Light drizzle"
            61, 63, 65, 66, 67, 80, 81, 82 -> WeatherConditionKind.RAIN to "Rain showers"
            71, 73, 75, 77, 85, 86 -> WeatherConditionKind.SNOW to "Snowfall"
            95, 96, 99 -> WeatherConditionKind.THUNDERSTORM to "Thunderstorm"
            else -> WeatherConditionKind.CLOUDY to "Cloudy"
        }

        fun buildTrailAdvisory(
            kind: WeatherConditionKind,
            tempC: Double,
            windKmh: Double,
            precipMm: Double
        ): Pair<String, WeatherAdvisoryLevel> = when {
            kind == WeatherConditionKind.THUNDERSTORM ->
                "Storm risk • Stay off exposed ridges & summits" to WeatherAdvisoryLevel.HAZARD
            kind == WeatherConditionKind.SNOW || tempC <= 0.0 ->
                "Freezing / snow • Pack warm layers & traction" to WeatherAdvisoryLevel.HAZARD
            kind == WeatherConditionKind.RAIN || precipMm >= 1.5 ->
                "Wet & slippery rock • Rain shell recommended" to WeatherAdvisoryLevel.CAUTION
            windKmh >= 35.0 ->
                "Strong ridge winds (${windKmh.roundToInt()} km/h) • Use caution" to WeatherAdvisoryLevel.CAUTION
            kind == WeatherConditionKind.FOG ->
                "Low visibility • Follow offline GPS route carefully" to WeatherAdvisoryLevel.CAUTION
            kind == WeatherConditionKind.DRIZZLE ->
                "Light mist/drizzle • Expect damp trail surfaces" to WeatherAdvisoryLevel.CAUTION
            tempC >= 32.0 ->
                "Warm trail conditions • Carry extra hydration" to WeatherAdvisoryLevel.CAUTION
            else ->
                "Favorable trail conditions • Clear visibility" to WeatherAdvisoryLevel.GOOD
        }
    }
}
