package com.sajdah.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.PrayerTimes
import com.sajdah.app.data.LocationRepository
import com.sajdah.app.utils.PrayerCalculationUtils
import com.sajdah.app.workers.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    val locationRepository: LocationRepository
) : ViewModel() {

    private val _prayerTimes = MutableStateFlow<PrayerTimes?>(null)
    val prayerTimes: StateFlow<PrayerTimes?> = _prayerTimes

    private val _currentPrayer = MutableStateFlow("")
    val currentPrayer: StateFlow<String> = _currentPrayer

    private val _nextPrayer = MutableStateFlow("")
    val nextPrayer: StateFlow<String> = _nextPrayer

    private val _countdown = MutableStateFlow("")
    val countdown: StateFlow<String> = _countdown

    init {
        viewModelScope.launch {
            locationRepository.currentLocation.collect { location ->
                loadPrayerTimes(location.latitude, location.longitude)
            }
        }
        
        viewModelScope.launch {
            locationRepository.fetchDeviceLocation()
        }
        
        startCountdownTimer()
    }

    private fun loadPrayerTimes(lat: Double, lng: Double) {
        val times = PrayerCalculationUtils.getPrayerTimes(lat, lng, Date())
        _prayerTimes.value = times
        updateCurrentAndNextPrayer(times)
        AlarmScheduler.schedulePrayerAlarms(context, times)
    }

    private fun updateCurrentAndNextPrayer(times: PrayerTimes) {
        _currentPrayer.value = times.currentPrayer().name
        _nextPrayer.value = times.nextPrayer().name
    }

    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (true) {
                _prayerTimes.value?.let { times ->
                    val nextPrayerTime = times.timeForPrayer(times.nextPrayer())
                    if (nextPrayerTime != null) {
                        val diff = nextPrayerTime.time - System.currentTimeMillis()
                        if (diff > 0) {
                            val hours = (diff / (1000 * 60 * 60)) % 24
                            val minutes = (diff / (1000 * 60)) % 60
                            val seconds = (diff / 1000) % 60
                            _countdown.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        } else {
                            val loc = locationRepository.currentLocation.value
                            loadPrayerTimes(loc.latitude, loc.longitude)
                        }
                    } else {
                        _countdown.value = "--:--:--"
                    }
                }
                delay(1000)
            }
        }
    }
}
