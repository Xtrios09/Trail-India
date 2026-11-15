package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.MapLayerStyle
import com.example.domain.model.OfflineMapRegion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.trailIndiaDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "trailindia_prefs"
)

class AppPreferencesRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val MAP_LAYER = stringPreferencesKey("map_layer")
        val OFFLINE_MAP_MODE = booleanPreferencesKey("offline_map_mode")
        val SHOW_PEAKS = booleanPreferencesKey("show_peaks")
        val SHOW_WEATHER = booleanPreferencesKey("show_weather_overlay")
        val SEARCH_HISTORY = stringPreferencesKey("search_history_json")
        val TRAIL_IMAGE_URLS = stringPreferencesKey("trail_image_urls_json")
        val OFFLINE_REGIONS = stringPreferencesKey("offline_regions_json")
    }

    val mapLayerStyle: Flow<MapLayerStyle> = context.trailIndiaDataStore.data.map { prefs ->
        val raw = prefs[Keys.MAP_LAYER]
        MapLayerStyle.entries.find { it.name == raw } ?: MapLayerStyle.OPENTOPOMAP
    }

    val isOfflineMapMode: Flow<Boolean> = context.trailIndiaDataStore.data.map { prefs ->
        prefs[Keys.OFFLINE_MAP_MODE] ?: false
    }

    val showPeakLabels: Flow<Boolean> = context.trailIndiaDataStore.data.map { prefs ->
        prefs[Keys.SHOW_PEAKS] ?: true
    }

    val showWeatherOverlay: Flow<Boolean> = context.trailIndiaDataStore.data.map { prefs ->
        prefs[Keys.SHOW_WEATHER] ?: true
    }

    val searchHistory: Flow<List<String>> = context.trailIndiaDataStore.data.map { prefs ->
        val raw = prefs[Keys.SEARCH_HISTORY] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
    }

    val trailImageUrls: Flow<Map<String, String>> = context.trailIndiaDataStore.data.map { prefs ->
        val raw = prefs[Keys.TRAIL_IMAGE_URLS] ?: return@map emptyMap()
        runCatching { json.decodeFromString<Map<String, String>>(raw) }.getOrDefault(emptyMap())
    }

    val offlineRegions: Flow<List<OfflineMapRegion>> = context.trailIndiaDataStore.data.map { prefs ->
        val raw = prefs[Keys.OFFLINE_REGIONS] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<OfflineMapRegion>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun setMapLayerStyle(style: MapLayerStyle) {
        context.trailIndiaDataStore.edit { prefs ->
            prefs[Keys.MAP_LAYER] = style.name
        }
    }

    suspend fun setOfflineMapMode(enabled: Boolean) {
        context.trailIndiaDataStore.edit { prefs ->
            prefs[Keys.OFFLINE_MAP_MODE] = enabled
        }
    }

    suspend fun setShowPeakLabels(enabled: Boolean) {
        context.trailIndiaDataStore.edit { prefs ->
            prefs[Keys.SHOW_PEAKS] = enabled
        }
    }

    suspend fun setShowWeatherOverlay(enabled: Boolean) {
        context.trailIndiaDataStore.edit { prefs ->
            prefs[Keys.SHOW_WEATHER] = enabled
        }
    }

    suspend fun addSearchQuery(query: String) {
        val clean = query.trim()
        if (clean.length < 2) return
        context.trailIndiaDataStore.edit { prefs ->
            val current = prefs[Keys.SEARCH_HISTORY]?.let { raw ->
                runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
            }.orEmpty()
            val updated = (listOf(clean) + current.filterNot { it.equals(clean, ignoreCase = true) })
                .take(8)
            prefs[Keys.SEARCH_HISTORY] = json.encodeToString(updated)
        }
    }

    suspend fun removeSearchQuery(query: String) {
        context.trailIndiaDataStore.edit { prefs ->
            val current = prefs[Keys.SEARCH_HISTORY]?.let { raw ->
                runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
            }.orEmpty()
            val updated = current.filterNot { it.equals(query, ignoreCase = true) }
            prefs[Keys.SEARCH_HISTORY] = json.encodeToString(updated)
        }
    }

    suspend fun clearSearchHistory() {
        context.trailIndiaDataStore.edit { prefs ->
            prefs.remove(Keys.SEARCH_HISTORY)
        }
    }

    suspend fun saveTrailImageUrls(urls: Map<String, String>) {
        if (urls.isEmpty()) return
        context.trailIndiaDataStore.edit { prefs ->
            val current = prefs[Keys.TRAIL_IMAGE_URLS]?.let { raw ->
                runCatching { json.decodeFromString<Map<String, String>>(raw) }.getOrDefault(emptyMap())
            }.orEmpty()
            val merged = current + urls
            prefs[Keys.TRAIL_IMAGE_URLS] = json.encodeToString(merged)
        }
    }

    suspend fun addOfflineRegion(region: OfflineMapRegion) {
        context.trailIndiaDataStore.edit { prefs ->
            val current = prefs[Keys.OFFLINE_REGIONS]?.let { raw ->
                runCatching { json.decodeFromString<List<OfflineMapRegion>>(raw) }.getOrDefault(emptyList())
            }.orEmpty()
            val updated = (listOf(region) + current.filterNot { it.id == region.id }).take(25)
            prefs[Keys.OFFLINE_REGIONS] = json.encodeToString(updated)
        }
    }

    suspend fun removeOfflineRegion(regionId: String) {
        context.trailIndiaDataStore.edit { prefs ->
            val current = prefs[Keys.OFFLINE_REGIONS]?.let { raw ->
                runCatching { json.decodeFromString<List<OfflineMapRegion>>(raw) }.getOrDefault(emptyList())
            }.orEmpty()
            val updated = current.filterNot { it.id == regionId }
            prefs[Keys.OFFLINE_REGIONS] = json.encodeToString(updated)
        }
    }

    suspend fun clearOfflineRegions() {
        context.trailIndiaDataStore.edit { prefs ->
            prefs.remove(Keys.OFFLINE_REGIONS)
        }
    }
}
