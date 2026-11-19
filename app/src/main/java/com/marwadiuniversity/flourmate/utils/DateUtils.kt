package com.marwadiuniversity.flourmate.utils

import java.util.Calendar
import java.util.TimeZone

object DateUtils {

    fun isTimestampInCurrentWeek(timestamp: Long): Boolean {
        if (timestamp <= 0) return false
        val tz = TimeZone.getTimeZone("Asia/Kolkata")
        val calNow = Calendar.getInstance(tz)
        val calOrder = Calendar.getInstance(tz)
        calOrder.timeInMillis = timestamp

        val currentYear = calNow.get(Calendar.YEAR)
        val currentWeek = calNow.get(Calendar.WEEK_OF_YEAR)

        val orderYear = calOrder.get(Calendar.YEAR)
        val orderWeek = calOrder.get(Calendar.WEEK_OF_YEAR)

        return currentYear == orderYear && currentWeek == orderWeek
    }
}