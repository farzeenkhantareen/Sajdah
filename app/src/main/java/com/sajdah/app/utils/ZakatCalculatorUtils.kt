package com.sajdah.app.utils

object ZakatCalculatorUtils {
    
    const val NISAB_GOLD_GRAMS = 87.48
    const val NISAB_SILVER_GRAMS = 612.36

    fun calculateZakat(
        cash: Double,
        goldValue: Double,
        silverValue: Double,
        businessAssets: Double,
        savings: Double,
        investments: Double,
        debts: Double,
        nisabThreshold: Double // Typically value of 87.48g gold or 612.36g silver
    ): ZakatResult {
        val totalWealth = cash + goldValue + silverValue + businessAssets + savings + investments
        val netWealth = totalWealth - debts
        
        val isEligible = netWealth >= nisabThreshold
        val zakatPayable = if (isEligible) netWealth * 0.025 else 0.0

        return ZakatResult(
            totalAssets = totalWealth,
            deductedDebts = debts,
            netWealth = netWealth,
            isEligible = isEligible,
            zakatAmount = zakatPayable
        )
    }
}

data class ZakatResult(
    val totalAssets: Double,
    val deductedDebts: Double,
    val netWealth: Double,
    val isEligible: Boolean,
    val zakatAmount: Double
)
