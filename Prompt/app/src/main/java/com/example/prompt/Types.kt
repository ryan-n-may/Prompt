package com.example.prompt

enum class DAYS(
    val dayNumber: Int,
    display: String,
) {
    MONDAY(1, "Monday"),
    TUESDAY(2, "Tuesday"),
    WEDNESDAY(3, "Wednesday"),
    THURSDAY(4, "Thursday"),
    FRIDAY(5, "Friday"),
    SATURDAY(6, "Saturday"),
    SUNDAY(7, "Sunday"),
}

enum class REPEATS(
    display: String,
) {
    NEVER("Never"),
    X_DAILY("Every _ days"),
    X_WEEKLY("Every _ weeks"),
    ON_DAYS("On days"),
    ON_DAYS_OF_MONTHS("On the _ day of the month"),
    ON_DAYS_INTO_MONTHS("On the first _ of the month"),
    ON_DATE_YEARLY("On this date yearly"),
}

val millisMins5 = 5 * 60 * 1000
val millisDay = 24 * 60 * 60 * 1000

typealias Time = String
