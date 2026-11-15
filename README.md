# TrailIndia

**TrailIndia** is an offline-first Android trekking discovery and GPS navigation app built for trails across India using **100% free and open-source data**—with no paid APIs or API keys required.

<table width="100%">
  <tr>
    <td width="33.33%">
     <img src="https://github.com/user-attachments/assets/d94807af-54ca-423c-9e39-77346f515e04" width="100%" />
    </td>
    <td width="33.33%">
      <img src="https://github.com/user-attachments/assets/82f2bdf5-b889-4564-8d47-176f2e7a910f" width="100%" />
    </td>
    <td width="33.33%">
      <img src="https://github.com/user-attachments/assets/2316f2fe-6a1e-4074-9ef0-512d61b2cc82" width="100%" />
    </td>
  </tr>
  <tr>
    <td width="33.33%">
      <img src="https://github.com/user-attachments/assets/ab2c8de1-e89b-4f4c-ac49-9401be41446e" width="100%" />
    </td>
    <td width="33.33%">
      <img src="https://github.com/user-attachments/assets/6eed092d-704a-4cdc-88a2-abe025eba77b" width="100%" />
    </td>
    <td width="33.33%">
      <img src="https://github.com/user-attachments/assets/a446e97c-faea-4bc1-aa0f-50e9b6f3786c" width="100%" />
    </td>
  </tr>
</table>

## Features

- **Pan-India Trail Discovery**: Browse curated routes across the **Himalayas**, **Northeast India**, **Eastern Ghats**, **Western Ghats**, and **South India**, with filters by region, state, difficulty, saved status, search history, and distance from your location.
- **Interactive OpenStreetMap Maps**: Switch between **OpenTopoMap**, **OSM Standard**, and **CyclOSM** layers, inspect peak markers, and sync nearby routes live via the OpenStreetMap **Overpass API**.
- **Offline Map Caching & Manual Region Selector**: Download map tiles along a trail corridor or use the interactive viewfinder selector to cache any custom bounding box at your chosen zoom level for zero-signal trekking.
- **Live GPS Follow-Along & Hike Recorder**: Track your real-time GPS position and compass heading along a route, record hikes offline (distance, duration, elevation gain, max altitude), and export tracks as **GPX** files.
- **Route Telemetry & Elevation Profiles**: View distance, elevation gain/range, estimated duration, waypoints, and interactive elevation charts.
- **Trailhead Weather & Real Photos**: Live trailhead weather advisories via **Open-Meteo** and location-matched trail photography via **Wikipedia / Wikimedia Commons** (cached locally for offline viewing).

## Tech Stack

- **Language & UI**: Kotlin, Jetpack Compose, Material Design 3
- **Maps & Location**: OSMDroid (`osmdroid-android`), Android Location & Sensor APIs
- **Persistence**: Room Database, Jetpack DataStore
- **Networking & Images**: Retrofit, OkHttp, Moshi, Coil
- **Async**: Kotlin Coroutines & StateFlow

## Getting Started

1. Clone the repository and open it in **Android Studio**.
2. Ensure **JDK 17+** and **Android SDK 36** (minimum supported SDK: **API 24 / Android 7.0**) are installed.
3. Sync Gradle and run the `app` configuration on an emulator or physical Android device—no API keys or extra configuration files are required.

## Data & Attribution

- Map tiles & trail/peak geometry: **© OpenStreetMap contributors**, **OpenTopoMap**, **CyclOSM**, and **Overpass API**
- Weather telemetry: **Open-Meteo**
- Trail photography: **Wikipedia / Wikimedia Commons**
