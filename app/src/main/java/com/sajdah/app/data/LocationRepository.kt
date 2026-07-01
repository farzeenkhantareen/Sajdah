package com.sajdah.app.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "location_prefs")

@Singleton
class LocationRepository @Inject constructor(
    private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private object Keys {
        val LAT  = doublePreferencesKey("location_lat")
        val LNG  = doublePreferencesKey("location_lng")
        val CITY = stringPreferencesKey("location_city")
    }

    // Default: Islamabad
    private val _currentLocation = MutableStateFlow(LocationData(33.6844, 73.0479, "Islamabad, PK"))
    val currentLocation: StateFlow<LocationData> = _currentLocation

    init {
        // Restore saved location from DataStore on startup
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = context.dataStore.data.first()
            val lat  = prefs[Keys.LAT]
            val lng  = prefs[Keys.LNG]
            val city = prefs[Keys.CITY]
            if (lat != null && lng != null && city != null) {
                _currentLocation.value = LocationData(lat, lng, city)
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchDeviceLocation() {
        try {
            val location = fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (location != null) {
                val city = getCityName(location.latitude, location.longitude)
                saveAndUpdate(location.latitude, location.longitude, city)
            }
        } catch (e: Exception) {
            // Permission not granted or GPS off – keep current value
        }
    }

    suspend fun setManualLocation(cityName: String) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(cityName, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val formatted = "${address.locality ?: address.subAdminArea ?: cityName}, ${address.countryCode ?: ""}"
                saveAndUpdate(address.latitude, address.longitude, formatted)
            }
        } catch (e: Exception) {
            // Geocoding failed
        }
    }

    /** Persists to DataStore AND updates the live StateFlow. */
    private suspend fun saveAndUpdate(lat: Double, lng: Double, city: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAT]  = lat
            prefs[Keys.LNG]  = lng
            prefs[Keys.CITY] = city
        }
        _currentLocation.value = LocationData(lat, lng, city)
    }

    private fun getCityName(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                "${address.locality ?: address.subAdminArea ?: "Unknown"}, ${address.countryCode ?: ""}"
            } else {
                "Loc: ${String.format("%.2f", lat)}, ${String.format("%.2f", lng)}"
            }
        } catch (e: Exception) {
            "Loc: ${String.format("%.2f", lat)}, ${String.format("%.2f", lng)}"
        }
    }
}

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)

