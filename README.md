# TrailIndia

**TrailIndia** is an offline-first Android trekking discovery and GPS navigation app built for trails across India using **100% free and open-source data**—with no paid APIs or API keys required.

<img width="275" height="600" alt="{19BF5B46-96FE-433C-9370-B7F4863E36CA}" src="https://github.com/user-attachments/assets/76752b07-a8e8-4531-9602-03cbb2650bab" />
<img width="275" height="600" alt="{2CB846A2-F3AC-4C10-A77C-52F972B5CB34}" src="https://github.com/user-attachments/assets/a5a296c1-c538-43a1-9460-93b8d6141448" />
<img width="275" height="600" alt="{7CF12E47-625B-440B-8E28-9F67DA6B845B}" src="https://github.com/user-attachments/assets/7ac2b350-5906-4c12-b76c-d96d2f302010" />


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
