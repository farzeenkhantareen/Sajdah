package com.sajdah.app.ui.screens.zakat

import androidx.lifecycle.ViewModel
import com.sajdah.app.utils.ZakatCalculatorUtils
import com.sajdah.app.utils.ZakatResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ZakatViewModel @Inject constructor() : ViewModel() {

    private val _zakatResult = MutableStateFlow<ZakatResult?>(null)
    val zakatResult: StateFlow<ZakatResult?> = _zakatResult

    fun calculateZakat(
        cash: Double, gold: Double, silver: Double,
        business: Double, savings: Double, investments: Double, debts: Double
    ) {
        val result = ZakatCalculatorUtils.calculateZakat(
            cash, gold, silver, business, savings, investments, debts,
            nisabThreshold = 500.0 // Simplified, ideally fetch actual threshold based on gold/silver price
        )
        _zakatResult.value = result
    }
}
