package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FilterDrama
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TrailWeather
import com.example.domain.model.TrailWeatherUiState
import com.example.domain.model.WeatherAdvisoryLevel
import com.example.domain.model.WeatherConditionKind
import com.example.ui.theme.DifficultyEasyBg
import com.example.ui.theme.DifficultyEasyFg
import com.example.ui.theme.DifficultyHardBg
import com.example.ui.theme.DifficultyHardFg
import com.example.ui.theme.DifficultyModerateBg
import com.example.ui.theme.DifficultyModerateFg
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MutedMossText
import com.example.ui.theme.WeatherRainTeal
import com.example.ui.theme.WeatherSkyBlue
import com.example.ui.theme.WeatherSnowIce
import com.example.ui.theme.WeatherStormAmber
import com.example.ui.theme.WeatherSunGold
import kotlin.math.roundToInt

@Composable
fun WeatherOverlayCard(
    weatherState: TrailWeatherUiState,
    visible: Boolean,
    onRefresh: () -> Unit,
    onHideOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val haptics = rememberTrailHaptics()

    AnimatedVisibility(
        visible = visible && weatherState !is TrailWeatherUiState.Idle,
        enter = fadeIn() + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        when (weatherState) {
            is TrailWeatherUiState.Idle -> Unit
            is TrailWeatherUiState.Loading -> {
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("map_weather_loading_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Checking ${weatherState.trailName} weather…",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            is TrailWeatherUiState.Unavailable -> {
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .clickable {
                            haptics.tap()
                            onRefresh()
                        }
                        .testTag("map_weather_unavailable_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (weatherState.isOffline) Icons.Rounded.CloudOff else Icons.Rounded.Refresh,
                            contentDescription = "Retry weather",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = weatherState.message,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            is TrailWeatherUiState.Ready -> {
                val weather = weatherState.weather
                val (icon, accentColor) = weatherVisuals(weather.conditionKind, weather.isDay)

                Surface(
                    shape = RoundedCornerShape(if (expanded) 20.dp else 99.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 5.dp,
                    modifier = Modifier
                        .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    if (!expanded) {
                        Row(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable {
                                    haptics.tap()
                                    expanded = true
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("map_weather_pill"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(accentColor.copy(alpha = 0.16f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = weather.conditionLabel,
                                    tint = accentColor,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Text(
                                text = "${weather.temperatureC.roundToInt()}°C",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = weather.conditionLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Air,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${weather.windSpeedKmh.roundToInt()} km/h",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (weather.advisoryLevel != WeatherAdvisoryLevel.GOOD) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = "Weather advisory",
                                    tint = if (weather.advisoryLevel == WeatherAdvisoryLevel.HAZARD) {
                                        DifficultyHardFg
                                    } else {
                                        WeatherStormAmber
                                    },
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.ExpandMore,
                                contentDescription = "Expand weather details",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        ExpandedWeatherContent(
                            weather = weather,
                            icon = icon,
                            accentColor = accentColor,
                            onRefresh = {
                                haptics.confirm()
                                onRefresh()
                            },
                            onCollapse = {
                                haptics.tap()
                                expanded = false
                            },
                            onHideOverlay = {
                                haptics.tap()
                                expanded = false
                                onHideOverlay()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedWeatherContent(
    weather: TrailWeather,
    icon: ImageVector,
    accentColor: Color,
    onRefresh: () -> Unit,
    onCollapse: () -> Unit,
    onHideOverlay: () -> Unit
) {
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            accentColor.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.surface
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgBrush)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("map_weather_expanded_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TRAILHEAD WEATHER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = buildString {
                        append(weather.trailName)
                        if (weather.elevationM != null) {
                            append(" • ${weather.elevationM} m")
                        }
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("map_weather_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh trail weather",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandLess,
                        contentDescription = "Collapse weather overlay",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(accentColor.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = weather.conditionLabel,
                    tint = accentColor,
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${weather.temperatureC.roundToInt()}°C",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = weather.conditionLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = "Feels like ${weather.apparentTempC.roundToInt()}°C",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeatherMetricTile(
                label = "WIND",
                value = "${weather.windSpeedKmh.roundToInt()} km/h",
                modifier = Modifier.weight(1f)
            )
            WeatherMetricTile(
                label = "HUMIDITY",
                value = "${weather.humidityPct}%",
                modifier = Modifier.weight(1f)
            )
            WeatherMetricTile(
                label = "PRECIP",
                value = String.format("%.1f mm", weather.precipitationMm),
                modifier = Modifier.weight(1f)
            )
            WeatherMetricTile(
                label = "CLOUDS",
                value = "${weather.cloudCoverPct}%",
                modifier = Modifier.weight(1f)
            )
        }

        val (advBg, advFg) = when (weather.advisoryLevel) {
            WeatherAdvisoryLevel.GOOD -> DifficultyEasyBg to DifficultyEasyFg
            WeatherAdvisoryLevel.CAUTION -> DifficultyModerateBg to DifficultyModerateFg
            WeatherAdvisoryLevel.HAZARD -> DifficultyHardBg to DifficultyHardFg
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = advBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (weather.advisoryLevel == WeatherAdvisoryLevel.GOOD) {
                        icon
                    } else {
                        Icons.Rounded.WarningAmber
                    },
                    contentDescription = null,
                    tint = advFg,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = weather.trailAdvisory,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = advFg
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val ageMinutes = ((System.currentTimeMillis() - weather.observedAtEpochMs) / 60_000L)
                .coerceAtLeast(0L)
            val ageText = when {
                ageMinutes == 0L -> "just now"
                ageMinutes == 1L -> "1m ago"
                else -> "${ageMinutes}m ago"
            }
            Text(
                text = if (weather.isFromCache) {
                    "Open-Meteo • Cached ($ageText)"
                } else {
                    "Open-Meteo Live • $ageText"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(
                onClick = onHideOverlay,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.VisibilityOff,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hide", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun WeatherMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

private fun weatherVisuals(kind: WeatherConditionKind, isDay: Boolean): Pair<ImageVector, Color> {
    return when (kind) {
        WeatherConditionKind.CLEAR ->
            (if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.NightsStay) to WeatherSunGold
        WeatherConditionKind.PARTLY_CLOUDY ->
            Icons.Rounded.FilterDrama to WeatherSkyBlue
        WeatherConditionKind.CLOUDY ->
            Icons.Rounded.Cloud to MutedMossText
        WeatherConditionKind.FOG ->
            Icons.Rounded.Cloud to MutedMossText
        WeatherConditionKind.DRIZZLE ->
            Icons.Rounded.Grain to WeatherRainTeal
        WeatherConditionKind.RAIN ->
            Icons.Rounded.WaterDrop to WeatherSkyBlue
        WeatherConditionKind.SNOW ->
            Icons.Rounded.AcUnit to WeatherSnowIce
        WeatherConditionKind.THUNDERSTORM ->
            Icons.Rounded.Thunderstorm to WeatherStormAmber
    }
}
