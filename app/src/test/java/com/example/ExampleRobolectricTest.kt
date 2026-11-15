package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.OpenMeteoWeatherClient
import com.example.data.remote.WikimediaTrailImageClient
import com.example.domain.model.WeatherAdvisoryLevel
import com.example.domain.model.WeatherConditionKind
import com.example.ui.viewmodel.TrailIndiaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TrailIndia", appName)
  }

  @Test
  fun `initializes TrailIndiaViewModel without crashing`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = TrailIndiaViewModel(app)
    assertNotNull(vm.uiState.value)
  }

  @Test
  fun `parse open meteo current weather payload`() {
    val sampleJson = """
      {
        "latitude": 32.25,
        "longitude": 76.33,
        "elevation": 2842.0,
        "current": {
          "temperature_2m": 8.4,
          "apparent_temperature": 5.9,
          "relative_humidity_2m": 64,
          "precipitation": 0.0,
          "weather_code": 2,
          "cloud_cover": 42,
          "wind_speed_10m": 14.5,
          "is_day": 1
        }
      }
    """.trimIndent()

    val weather = OpenMeteoWeatherClient.parseOpenMeteoJson(
      rawJson = sampleJson,
      trailId = "triund",
      trailName = "Triund Trek",
      fallbackLat = 32.2425,
      fallbackLon = 76.3323,
      observedAtEpochMs = 1_700_000_000_000L
    )

    assertEquals("triund", weather.trailId)
    assertEquals("Triund Trek", weather.trailName)
    assertEquals(2842, weather.elevationM)
    assertEquals(8.4, weather.temperatureC, 0.01)
    assertEquals(WeatherConditionKind.PARTLY_CLOUDY, weather.conditionKind)
    assertEquals(WeatherAdvisoryLevel.GOOD, weather.advisoryLevel)
    assertTrue(weather.isDay)
  }

  @Test
  fun `parse wikipedia pageimages and wikimedia commons json`() {
    val wikiJson = """
      {
        "query": {
          "redirects": [{"from": "Goechala", "to": "Goecha La"}],
          "pages": [
            {
              "title": "Triund",
              "thumbnail": {"source": "https://upload.wikimedia.org/wikipedia/commons/thumb/1/12/Triund_meadow.jpg/900px-Triund_meadow.jpg"}
            },
            {
              "title": "Goecha La",
              "thumbnail": {"source": "https://upload.wikimedia.org/wikipedia/commons/thumb/3/34/Goecha_La.jpg/900px-Goecha_La.jpg"}
            }
          ]
        }
      }
    """.trimIndent()

    val parsedWiki = WikimediaTrailImageClient.parseWikipediaPageImagesJson(wikiJson)
    assertEquals(
      "https://upload.wikimedia.org/wikipedia/commons/thumb/1/12/Triund_meadow.jpg/900px-Triund_meadow.jpg",
      parsedWiki["Triund"]
    )
    assertEquals(
      "https://upload.wikimedia.org/wikipedia/commons/thumb/3/34/Goecha_La.jpg/900px-Goecha_La.jpg",
      parsedWiki["Goechala"]
    )

    val commonsJson = """
      {
        "query": {
          "pages": [
            {
              "title": "File:Locator_map_example.svg",
              "imageinfo": [{"mime": "image/svg+xml", "thumburl": "https://upload.wikimedia.org/example.svg"}]
            },
            {
              "title": "File:Kudremukh_peak.jpg",
              "imageinfo": [{"mime": "image/jpeg", "thumburl": "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ab/Kudremukh_peak.jpg/900px-Kudremukh_peak.jpg"}]
            }
          ]
        }
      }
    """.trimIndent()

    val commonsUrl = WikimediaTrailImageClient.parseCommonsImageSearchJson(commonsJson)
    assertEquals(
      "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ab/Kudremukh_peak.jpg/900px-Kudremukh_peak.jpg",
      commonsUrl
    )
  }
}
