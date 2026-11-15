package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.remote.TrailImageLoader
import com.example.ui.theme.ForestDeep
import com.example.ui.theme.PineGreen

@Composable
fun TrailPhotoImage(
    imageUrl: String?,
    @DrawableRes fallbackDrawable: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val imageLoader = remember(context) { TrailImageLoader.get(context) }

    if (imageUrl.isNullOrBlank()) {
        Image(
            painter = painterResource(id = fallbackDrawable),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
        return
    }

    var loadFailed by remember(imageUrl) { mutableStateOf(false) }

    if (loadFailed) {
        Image(
            painter = painterResource(id = fallbackDrawable),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
        return
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            PineGreen.copy(alpha = 0.55f),
                            ForestDeep.copy(alpha = 0.85f)
                        )
                    )
                )
        )
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            imageLoader = imageLoader,
            contentDescription = contentDescription,
            contentScale = contentScale,
            onError = { loadFailed = true },
            modifier = Modifier.fillMaxSize()
        )
    }
}
