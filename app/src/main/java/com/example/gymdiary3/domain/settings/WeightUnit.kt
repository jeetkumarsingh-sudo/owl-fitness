package com.example.gymdiary3.domain.settings

import java.util.Locale

enum class WeightUnit(
    val preferenceValue: String,
    val symbol: String,
    val defaultStep: Double,
) {
    Kilograms("kg", "kg", 2.5),
    Pounds("lbs", "lbs", 5.0);

    fun fromKilograms(value: Double): Double =
        when (this) {
            Kilograms -> value
            Pounds -> value * KG_TO_LBS
        }

    fun toKilograms(value: Double): Double =
        when (this) {
            Kilograms -> value
            Pounds -> value / KG_TO_LBS
        }

    companion object {
        private const val KG_TO_LBS = 2.20462262185

        fun fromPreference(value: String?): WeightUnit =
            when (value?.lowercase(Locale.US)) {
                Pounds.preferenceValue -> Pounds
                else -> Kilograms
            }
    }
}

object WeightFormatter {
    fun label(unit: String): String = WeightUnit.fromPreference(unit).symbol

    fun fromKilograms(value: Double, unit: String): Double =
        WeightUnit.fromPreference(unit).fromKilograms(value)

    fun toKilograms(value: Double, unit: String): Double =
        WeightUnit.fromPreference(unit).toKilograms(value)

    fun step(unit: String): Double = WeightUnit.fromPreference(unit).defaultStep

    fun formatFromKilograms(
        value: Double,
        unit: String,
        decimals: Int = 1,
        includeUnit: Boolean = true,
    ): String {
        val convertedValue = fromKilograms(value, unit)
        val formattedValue = formatNumber(convertedValue, decimals)
        return if (includeUnit) "$formattedValue ${label(unit)}" else formattedValue
    }

    fun formatNumber(value: Double, decimals: Int = 1): String =
        "%.${decimals}f".format(Locale.US, value)
}
