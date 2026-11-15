package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.domain.model.LiveGpsState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class LocationTracker(private val context: Context) {

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun observeLocationAndCompass(): Flow<LiveGpsState> = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        var currentState = LiveGpsState(
            hasLocationPermission = hasLocationPermission(),
            isGpsEnabled = runCatching {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }.getOrDefault(false)
        )
        trySend(currentState)

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentState = currentState.copy(
                    hasLocationPermission = true,
                    isGpsEnabled = true,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitudeM = if (location.hasAltitude()) location.altitude else currentState.altitudeM,
                    accuracyM = if (location.hasAccuracy()) location.accuracy else null,
                    speedMps = if (location.hasSpeed()) location.speed else null,
                    lastFixEpochMs = System.currentTimeMillis()
                )
                trySend(currentState)
            }

            override fun onProviderEnabled(provider: String) {
                currentState = currentState.copy(isGpsEnabled = true)
                trySend(currentState)
            }

            override fun onProviderDisabled(provider: String) {
                val stillEnabled = runCatching {
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                }.getOrDefault(false)
                currentState = currentState.copy(isGpsEnabled = stillEnabled)
                trySend(currentState)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        var lastCompassEmitMs = 0L

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val now = System.currentTimeMillis()
                    if (now - lastCompassEmitMs < 250L) return
                    lastCompassEmitMs = now
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val azimuthDeg = ((Math.toDegrees(orientationAngles[0].toDouble()) + 360.0) % 360.0).toFloat()
                    currentState = currentState.copy(compassAzimuthDeg = azimuthDeg)
                    trySend(currentState)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (hasLocationPermission()) {
            val lastKnownGps = runCatching { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()
            val lastKnownNet = runCatching { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
            val bestLast = listOfNotNull(lastKnownGps, lastKnownNet).maxByOrNull { it.time }
            if (bestLast != null) {
                currentState = currentState.copy(
                    latitude = bestLast.latitude,
                    longitude = bestLast.longitude,
                    altitudeM = if (bestLast.hasAltitude()) bestLast.altitude else null,
                    accuracyM = if (bestLast.hasAccuracy()) bestLast.accuracy else null,
                    lastFixEpochMs = bestLast.time
                )
                trySend(currentState)
            }

            if (locationManager.allProviders.contains(LocationManager.GPS_PROVIDER)) {
                runCatching {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        3f,
                        locationListener,
                        Looper.getMainLooper()
                    )
                }
            }
            if (locationManager.allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                runCatching {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        5000L,
                        10f,
                        locationListener,
                        Looper.getMainLooper()
                    )
                }
            }
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose {
            runCatching { locationManager.removeUpdates(locationListener) }
            runCatching { sensorManager.unregisterListener(sensorListener) }
        }
    }.distinctUntilChanged()
}
