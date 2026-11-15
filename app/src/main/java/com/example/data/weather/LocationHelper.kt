package com.example.data.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlin.math.abs

object LocationHelper {
  // Primary Kurdistan Fallback Coordinates (Duhok)
  const val DEFAULT_DUHOK_LAT = 36.8665
  const val DEFAULT_DUHOK_LON = 42.9880

  // Secondary Kurdistan Fallback Coordinates (Erbil)
  const val DEFAULT_ERBIL_LAT = 36.1911
  const val DEFAULT_ERBIL_LON = 44.0091

  fun hasLocationPermission(context: Context): Boolean {
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

  fun getLastKnownCoordinates(context: Context): Pair<Double, Double>? {
    if (!hasLocationPermission(context)) return null
    return try {
      val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
      val providers = lm.getProviders(true)
      var bestLocation: Location? = null
      for (provider in providers) {
        val loc = try {
          lm.getLastKnownLocation(provider)
        } catch (e: SecurityException) {
          null
        }
        if (loc != null) {
          if (bestLocation == null || loc.time > bestLocation.time) {
            bestLocation = loc
          }
        }
      }
      bestLocation?.let { Pair(it.latitude, it.longitude) }
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Identifies nearby Kurdish/Regional city or returns generic Live Location label.
   */
  fun resolveCityName(lat: Double, lon: Double): Pair<String, String> {
    // Check known regional hubs within ~40-60 km threshold
    if (abs(lat - 36.8665) < 0.35 && abs(lon - 42.9880) < 0.40) {
      return Pair("Duhok", "دهۆک")
    }
    if (abs(lat - 36.1911) < 0.35 && abs(lon - 44.0091) < 0.40) {
      return Pair("Erbil", "هەولێر")
    }
    if (abs(lat - 35.5668) < 0.35 && abs(lon - 45.4161) < 0.40) {
      return Pair("Sulaymaniyah", "سلێمانی")
    }
    if (abs(lat - 37.1438) < 0.25 && abs(lon - 42.6874) < 0.30) {
      return Pair("Zakho", "زاخۆ")
    }
    if (abs(lat - 36.7450) < 0.25 && abs(lon - 43.8933) < 0.30) {
      return Pair("Akre", "ئاکرێ")
    }
    if (abs(lat - 37.0911) < 0.25 && abs(lon - 43.4883) < 0.30) {
      return Pair("Amedi", "ئامێدی")
    }
    // Kurdistan broader bounds check (34.0° to 38.0° N, 41.5° to 46.5° E)
    if (lat in 34.0..38.0 && lon in 41.5..46.5) {
      return Pair("Kurdistan", "کوردستان")
    }
    return Pair("Live Location", "جهێ تە یێ نوکە")
  }
}
