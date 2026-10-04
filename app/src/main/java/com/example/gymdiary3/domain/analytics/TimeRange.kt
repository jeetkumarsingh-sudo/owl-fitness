package com.example.gymdiary3.domain.analytics

/** Chart windows. [days] null = all recorded history. */
enum class TimeRange(val label: String, val days: Int?) {
    W4("4W", 28), W8("8W", 56), M3("3M", 91), M6("6M", 182), Y1("1Y", 365), ALL("All", null),
    D7("7D", 7), D30("30D", 30);

    /** Window start: [days] back from [now], or the first data point for ALL (or if data is newer). */
    fun start(now: Long, earliestData: Long?): Long {
        val d = days
        return if (d == null) (earliestData ?: (now - 28 * DAY_MS)) - DAY_MS / 2
        else now - d * DAY_MS
    }

    companion object {
        val EXERCISE = listOf(W4, W8, M3, M6, Y1, ALL)
        val BODY_WEIGHT = listOf(D7, D30, M3, M6, Y1)
        const val DAY_MS = 24L * 60 * 60 * 1000
    }
}
