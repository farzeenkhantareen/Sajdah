package com.sajdah.app.utils

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar
import java.util.Date

object PrayerCalculationUtils {
    
    fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        date: Date = Date(),
        method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE
    ): PrayerTimes {
        val coordinates = Coordinates(latitude, longitude)
        val cal = Calendar.getInstance()
        cal.time = date
        val dateComponents = DateComponents.from(date)
        val parameters = method.parameters
        
        return PrayerTimes(coordinates, dateComponents, parameters)
    }

    fun getCalculationMethod(methodName: String): CalculationMethod {
        return when(methodName) {
            "MUSLIM_WORLD_LEAGUE" -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            "KARACHI" -> CalculationMethod.KARACHI
            "ISNA" -> CalculationMethod.NORTH_AMERICA
            "UMM_AL_QURA" -> CalculationMethod.UMM_AL_QURA
            "EGYPT" -> CalculationMethod.EGYPTIAN
            else -> CalculationMethod.MUSLIM_WORLD_LEAGUE
        }
    }
}
