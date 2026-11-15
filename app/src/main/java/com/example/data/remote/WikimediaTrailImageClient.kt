package com.example.data.remote

import com.example.domain.model.Trail
import com.example.domain.model.TrekRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WikimediaTrailImageClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    suspend fun resolveCuratedTrailAndRegionImages(
        trails: List<Trail>,
        existingCache: Map<String, String>
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val resolved = existingCache.toMutableMap()

        // Build title -> keys map for trails and regions missing from cache
        val titleToKeys = linkedMapOf<String, MutableList<String>>()
        CURATED_WIKI_TITLES.forEach { (trailId, title) ->
            if (resolved[trailId].isNullOrBlank()) {
                titleToKeys.getOrPut(title) { mutableListOf() }.add(trailId)
            }
        }
        REGION_WIKI_TITLES.forEach { (region, title) ->
            val key = regionKey(region)
            if (resolved[key].isNullOrBlank()) {
                titleToKeys.getOrPut(title) { mutableListOf() }.add(key)
            }
        }

        // Batch query Wikipedia pageimages (up to 50 titles per request)
        if (titleToKeys.isNotEmpty()) {
            titleToKeys.keys.chunked(25).forEach { titleBatch ->
                runCatching {
                    val url = "https://en.wikipedia.org/w/api.php".toHttpUrl()
                        .newBuilder()
                        .addQueryParameter("action", "query")
                        .addQueryParameter("titles", titleBatch.joinToString("|"))
                        .addQueryParameter("prop", "pageimages")
                        .addQueryParameter("piprop", "thumbnail")
                        .addQueryParameter("pithumbsize", "900")
                        .addQueryParameter("redirects", "1")
                        .addQueryParameter("format", "json")
                        .addQueryParameter("formatversion", "2")
                        .build()

                    val req = Request.Builder()
                        .url(url)
                        .header("User-Agent", USER_AGENT)
                        .get()
                        .build()

                    client.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val json = resp.body?.string().orEmpty()
                            if (json.isNotBlank()) {
                                val parsed = parseWikipediaPageImagesJson(json)
                                parsed.forEach { (origTitle, thumbUrl) ->
                                    titleToKeys[origTitle]?.forEach { targetKey ->
                                        resolved[targetKey] = thumbUrl
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fallback to Wikimedia Commons geosearch for any trail still missing an image
        val missingTrails = trails.filter { resolved[it.id].isNullOrBlank() }
        missingTrails.take(10).forEach { trail ->
            fetchCommonsGeoPhoto(trail.trailheadLat, trail.trailheadLon, trail.name)?.let { imgUrl ->
                resolved[trail.id] = imgUrl
            }
        }

        resolved
    }

    suspend fun resolveSingleTrailImage(trail: Trail): String? = withContext(Dispatchers.IO) {
        val wikiTitle = CURATED_WIKI_TITLES[trail.id]
        if (wikiTitle != null) {
            runCatching {
                val url = "https://en.wikipedia.org/w/api.php".toHttpUrl()
                    .newBuilder()
                    .addQueryParameter("action", "query")
                    .addQueryParameter("titles", wikiTitle)
                    .addQueryParameter("prop", "pageimages")
                    .addQueryParameter("piprop", "thumbnail")
                    .addQueryParameter("pithumbsize", "900")
                    .addQueryParameter("redirects", "1")
                    .addQueryParameter("format", "json")
                    .addQueryParameter("formatversion", "2")
                    .build()

                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val json = resp.body?.string().orEmpty()
                        parseWikipediaPageImagesJson(json)[wikiTitle]
                    } else null
                }
            }.getOrNull()?.let { return@withContext it }
        }

        fetchCommonsGeoPhoto(trail.trailheadLat, trail.trailheadLon, trail.name)
    }

    private fun fetchCommonsGeoPhoto(lat: Double, lon: Double, trailName: String): String? {
        runCatching {
            val url = "https://commons.wikimedia.org/w/api.php".toHttpUrl()
                .newBuilder()
                .addQueryParameter("action", "query")
                .addQueryParameter("generator", "geosearch")
                .addQueryParameter("ggsnamespace", "6")
                .addQueryParameter("ggscoord", "$lat|$lon")
                .addQueryParameter("ggsradius", "10000")
                .addQueryParameter("ggslimit", "8")
                .addQueryParameter("prop", "imageinfo")
                .addQueryParameter("iiprop", "url|mime")
                .addQueryParameter("iiurlwidth", "900")
                .addQueryParameter("format", "json")
                .addQueryParameter("formatversion", "2")
                .build()

            val req = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val json = resp.body?.string().orEmpty()
                    parseCommonsImageSearchJson(json)?.let { return it }
                }
            }
        }

        // Secondary fallback: Wikimedia Commons file search by trail name
        return runCatching {
            val cleanQuery = trailName.replace("—", " ").replace("-", " ").trim()
            val url = "https://commons.wikimedia.org/w/api.php".toHttpUrl()
                .newBuilder()
                .addQueryParameter("action", "query")
                .addQueryParameter("generator", "search")
                .addQueryParameter("gsrnamespace", "6")
                .addQueryParameter("gsrsearch", "$cleanQuery India")
                .addQueryParameter("gsrlimit", "6")
                .addQueryParameter("prop", "imageinfo")
                .addQueryParameter("iiprop", "url|mime")
                .addQueryParameter("iiurlwidth", "900")
                .addQueryParameter("format", "json")
                .addQueryParameter("formatversion", "2")
                .build()

            val req = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    parseCommonsImageSearchJson(resp.body?.string().orEmpty())
                } else null
            }
        }.getOrNull()
    }

    companion object {
        private const val USER_AGENT = "TrailIndia-Android/1.0 (Open-Source Trekking App)"

        fun regionKey(region: TrekRegion): String = "region_${region.name}"

        val CURATED_WIKI_TITLES: Map<String, String> = mapOf(
            "him_triund" to "Triund",
            "him_kedarkantha" to "Kedarkantha",
            "him_valley_of_flowers" to "Valley of Flowers National Park",
            "him_hampta_pass" to "Hampta Pass",
            "him_markha_valley" to "Markha Valley",
            "him_goechala" to "Goechala",
            "ne_dzukou" to "Dzükou Valley",
            "ne_nongriat" to "Living root bridge",
            "ne_sandakphu" to "Sandakphu",
            "eg_deomali" to "Deomali (Odisha)",
            "eg_arma_konda" to "Arma Konda",
            "eg_swamimalai_yelagiri" to "Yelagiri",
            "wg_kalsubai" to "Kalsubai",
            "wg_harishchandragad" to "Harishchandragad",
            "wg_kudremukh" to "Kudremukh",
            "wg_tadiandamol" to "Tadiandamol",
            "si_meesapulimala" to "Meesapulimala",
            "si_chembra" to "Chembra Peak",
            "si_velliangiri" to "Velliangiri Mountains"
        )

        val REGION_WIKI_TITLES: Map<TrekRegion, String> = mapOf(
            TrekRegion.HIMALAYAS to "Garhwal Himalayas",
            TrekRegion.NORTHEAST_INDIA to "Dzükou Valley",
            TrekRegion.EASTERN_GHATS to "Eastern Ghats",
            TrekRegion.WESTERN_GHATS to "Western Ghats",
            TrekRegion.SOUTH_INDIA to "Nilgiri Mountains"
        )

        fun parseWikipediaPageImagesJson(rawJson: String): Map<String, String> {
            if (rawJson.isBlank()) return emptyMap()
            val root = JSONObject(rawJson)
            val query = root.optJSONObject("query") ?: return emptyMap()

            // Reverse map canonical title -> original requested title(s)
            val normalizedMap = mutableMapOf<String, String>()
            query.optJSONArray("normalized")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val item = arr.optJSONObject(i) ?: continue
                    val from = item.optString("from")
                    val to = item.optString("to")
                    if (from.isNotBlank() && to.isNotBlank()) {
                        normalizedMap[to] = from
                    }
                }
            }
            val redirectMap = mutableMapOf<String, String>()
            query.optJSONArray("redirects")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val item = arr.optJSONObject(i) ?: continue
                    val from = item.optString("from")
                    val to = item.optString("to")
                    if (from.isNotBlank() && to.isNotBlank()) {
                        redirectMap[to] = normalizedMap[from] ?: from
                    }
                }
            }

            val result = mutableMapOf<String, String>()
            val pages = query.optJSONArray("pages") ?: return emptyMap()
            for (i in 0 until pages.length()) {
                val page = pages.optJSONObject(i) ?: continue
                if (page.optBoolean("missing", false)) continue
                val title = page.optString("title")
                val thumb = page.optJSONObject("thumbnail")?.optString("source").orEmpty()
                if (title.isBlank() || thumb.isBlank() || !isLikelyRealPhotoUrl(thumb)) continue

                val originalTitle = redirectMap[title] ?: normalizedMap[title] ?: title
                result[originalTitle] = thumb
                result[title] = thumb
            }
            return result
        }

        fun parseCommonsImageSearchJson(rawJson: String): String? {
            if (rawJson.isBlank()) return null
            val root = JSONObject(rawJson)
            val pages = root.optJSONObject("query")?.optJSONArray("pages") ?: return null
            for (i in 0 until pages.length()) {
                val page = pages.optJSONObject(i) ?: continue
                val title = page.optString("title")
                val infoArr = page.optJSONArray("imageinfo") ?: continue
                val info = infoArr.optJSONObject(0) ?: continue
                val mime = info.optString("mime")
                val thumbUrl = info.optString("thumburl").ifBlank { info.optString("url") }
                if (mime.startsWith("image/jpeg") || mime.startsWith("image/png")) {
                    if (isLikelyRealPhotoUrl(thumbUrl) && isLikelyRealPhotoUrl(title)) {
                        return thumbUrl
                    }
                }
            }
            return null
        }

        private fun isLikelyRealPhotoUrl(value: String): Boolean {
            val lower = value.lowercase()
            return !lower.endsWith(".svg") &&
                !lower.endsWith(".pdf") &&
                !lower.contains("flag_of_") &&
                !lower.contains("locator_map") &&
                !lower.contains("coat_of_arms") &&
                !lower.contains("emblem")
        }
    }
}
