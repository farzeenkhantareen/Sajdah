package com.sajdah.app.ui.screens.qibla

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajdah.app.data.LocationRepository
import com.sajdah.app.utils.QiblaManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val context: Context,
    val locationRepository: LocationRepository
) : ViewModel() {

    private val qiblaManager = QiblaManager(context)

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth

    private val _qiblaDirection = MutableStateFlow(0f)
    val qiblaDirection: StateFlow<Float> = _qiblaDirection

    init {
        qiblaManager.start()
        viewModelScope.launch {
            qiblaManager.azimuth.collect { angle ->
                _azimuth.value = angle
            }
        }
        
        viewModelScope.launch {
            locationRepository.currentLocation.collect { location ->
                _qiblaDirection.value = com.batoulapps.adhan.Qibla(
                    com.batoulapps.adhan.Coordinates(location.latitude, location.longitude)
                ).direction.toFloat()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        qiblaManager.stop()
    }
}
