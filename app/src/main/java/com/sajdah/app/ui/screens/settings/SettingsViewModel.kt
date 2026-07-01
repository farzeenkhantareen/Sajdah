package com.sajdah.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajdah.app.data.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val locationRepository: LocationRepository
) : ViewModel() {

    private val _calculationMethod = MutableStateFlow("MUSLIM_WORLD_LEAGUE")
    val calculationMethod: StateFlow<String> = _calculationMethod

    fun updateCalculationMethod(method: String) {
        _calculationMethod.value = method
    }

    fun updateLocationManually(cityName: String) {
        viewModelScope.launch {
            locationRepository.setManualLocation(cityName)
        }
    }
}
