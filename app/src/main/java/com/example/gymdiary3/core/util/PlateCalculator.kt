package com.example.gymdiary3.core.util

/**
 * Pure, unit-agnostic barbell plate calculation.
 *
 * All weights passed in must share the same unit (kg or lbs); the returned plate
 * denominations are whatever list of [plates] the caller supplies for that unit.
 */
object PlateCalculator {

    /** Standard metric plates (kg), heaviest first. */
    val KG_PLATES = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

    /** Standard imperial plates (lbs), heaviest first. */
    val LBS_PLATES = listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5)

    data class PlateCount(val plate: Double, val count: Int)

    /**
     * Returns the plates to load on ONE side of the bar to reach [targetWeight],
     * greedily from heaviest to lightest. Returns an empty list when the target is
     * at or below the bar weight (nothing to load).
     */
    fun platesPerSide(
        targetWeight: Double,
        barWeight: Double,
        plates: List<Double>,
    ): List<PlateCount> {
        val sideLoad = (targetWeight - barWeight) / 2.0
        if (sideLoad <= 0.0) return emptyList()

        var remaining = sideLoad
        val result = mutableListOf<PlateCount>()
        for (plate in plates) {
            if (plate <= 0.0) continue
            val count = (remaining / plate).toInt()
            if (count > 0) {
                result.add(PlateCount(plate, count))
                remaining -= count * plate
            }
        }
        return result
    }

    /** Convenience: pick the conventional plate set for a unit ("kg" or "lbs"). */
    fun platesForUnit(unit: String): List<Double> =
        if (unit.equals("lbs", ignoreCase = true)) LBS_PLATES else KG_PLATES
}
