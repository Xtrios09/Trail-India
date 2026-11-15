package com.example.data.remote

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

object TrailImageLoader {

    @Volatile
    private var imageLoader: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return imageLoader ?: synchronized(this) {
            imageLoader ?: buildLoader(context.applicationContext).also { imageLoader = it }
        }
    }

    fun prefetch(context: Context, imageUrl: String) {
        if (imageUrl.isBlank()) return
        val appCtx = context.applicationContext
        val request = ImageRequest.Builder(appCtx)
            .data(imageUrl)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
        get(appCtx).enqueue(request)
    }

    private fun buildLoader(appContext: Context): ImageLoader {
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "TrailIndia-Android/1.0 (Open-Source Trekking App)")
                    .build()
                chain.proceed(req)
            }
            .build()

        return ImageLoader.Builder(appContext)
            .okHttpClient(okHttp)
            .memoryCache {
                MemoryCache.Builder(appContext)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(appContext.filesDir, "trail_photos"))
                    .maxSizeBytes(60L * 1024L * 1024L)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}
