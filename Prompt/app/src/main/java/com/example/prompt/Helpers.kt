package com.example.prompt

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun getCurrentDate(): Date {
    val now: Date = Date()
    return now
}

fun isTheDayInclusiveOf(days: MutableList<DAYS>): Boolean {
    val cal1 = Calendar.getInstance()
    val weekDay = cal1.get(Calendar.DAY_OF_WEEK)
    val validDays =
        days.find {
            it.dayNumber == weekDay
        }

    if (validDays == null) {
        return false
    }
    return true
}

fun isDayOfMonth(dayOfMonth: Int): Boolean {
    val cal1 = Calendar.getInstance()
    val day = cal1.get(Calendar.DAY_OF_MONTH)
    return day == dayOfMonth
}

fun isFirstXsOfMonth(day: MutableList<DAYS>): Boolean {
    val cal1 = Calendar.getInstance()
    val dayOfWeek = cal1.get(Calendar.DAY_OF_WEEK)
    val dayOfMonth = cal1.get(Calendar.DAY_OF_MONTH)

    fun isDayLegit(it: DAYS): Boolean = dayOfWeek == it.dayNumber && dayOfMonth <= 7
    val result = day.find(::isDayLegit)
    if (result == null) {
        return false
    }
    return true
}

fun isDifferentCalendarWeek(
    date: Date,
    weeksDown: Int = 1,
): Boolean {
    val cal1 = Calendar.getInstance()
    cal1.time = date

    val cal2 = Calendar.getInstance() // Now

    val xWeeksDown = cal1.get(Calendar.WEEK_OF_YEAR) - cal2.get(Calendar.WEEK_OF_YEAR) == weeksDown

    return xWeeksDown
}

fun isDifferentCalendarDay(
    date: Date,
    daysDown: Int = 1,
): Boolean {
    val cal1 = Calendar.getInstance()
    cal1.time = date

    val cal2 = Calendar.getInstance() // Now

    val xDaysDown = cal1.get(Calendar.DAY_OF_YEAR) - cal2.get(Calendar.DAY_OF_YEAR) == daysDown

    return xDaysDown
}

fun parseIso8601(isoString: Time): Date {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.parse(isoString)!!
}
