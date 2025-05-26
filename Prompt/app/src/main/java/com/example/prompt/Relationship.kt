package com.example.prompt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import java.util.Date
import kotlin.math.abs

data class RelationshipConfiguration(
    var repeats: REPEATS = REPEATS.NEVER,
    var repetitionInt: Int? = 1,
    var days: MutableList<DAYS?> = mutableListOf(DAYS.MONDAY),
    var intoMonth: Int? = 1,
)

class Relationship(
    var name: String,
    var desc: String,
    private var offset: Long,
    private var config: RelationshipConfiguration,
    var trigger: Trigger? = null,
) {
    private val disabled: Boolean = false
    private val scope = CoroutineScope(Dispatchers.Default)
    private val subscriber = EventSubscriber("${name}_subscriber", scope)

    private var lastTriggered: Date? = null

    // Improves performance by caching the renotify status daily- cache is cleared each calendar day.
    private var canWeRenotifyHold: Boolean? = null

    /**
     * Computes whether we are able to renotify the device as per their configuration.
     */
    private fun canWeRenotify(): Boolean {
        var canWeRenotify = false
        if (disabled) {
            // simple lockout condition never to renotify disabled relationships.
            canWeRenotify = false
        } else if (lastTriggered == null) {
            // if last triggered is not set, we can trigger.
            canWeRenotify = true
        } else if (this.config.repeats == REPEATS.NEVER) {
            // if we never repeat, we never renotify.
            canWeRenotify = false
        } else if (canWeRenotifyHold != null) {
            // if we have a cached version of canWeRenotify hold and the last trigger was less than
            // one day ago, we can use this cached value.
            val isDaysDown = isDifferentCalendarDay(lastTriggered!!, 0)
            if (isDaysDown) {
                canWeRenotify = canWeRenotifyHold!!
            }
        } else if (this.config.repeats == REPEATS.X_DAILY) {
            // check X days have passed since last notification
            val isDaysDown =
                this.config.repetitionInt?.let { isDifferentCalendarDay(lastTriggered!!, it) } ?: false
            canWeRenotify = isDaysDown
        } else if (this.config.repeats == REPEATS.X_WEEKLY) {
            // check X weeks have passed since last notification
            val isWeeksDown =
                this.config.repetitionInt?.let {
                    isDifferentCalendarWeek(
                        lastTriggered!!,
                        it,
                    )
                } ?: false
            canWeRenotify = isWeeksDown
        } else if (this.config.repeats == REPEATS.ON_DAYS) {
            // check we are on an acceptable day for the given notification
            if (this.config.days.contains(null)) {
                Timber.w("Repeat on days contains a null day- skipping condition")
                return false
            }
            canWeRenotify = isTheDayInclusiveOf(this.config.days as MutableList<DAYS>)
        } else if (this.config.repeats == REPEATS.ON_DAYS_INTO_MONTHS) {
            // check that we are on the correct day into the month to notify.
            if (this.config.intoMonth == null) {
                Timber.w("Days into month is null- skipping condition")
            }
            canWeRenotify = isDayOfMonth(this.config.intoMonth!!)
        } else if (this.config.repeats == REPEATS.ON_DAYS_OF_MONTHS) {
            // check that we are on the first XXXday of the month, given an array, to notify.
            if (this.config.days == null) {
                Timber.w("Days into month is null- skipping condition")
            }
            canWeRenotify = isFirstXsOfMonth(this.config.days as MutableList<DAYS>)
        }

        // re-cache the latest result.
        canWeRenotifyHold = canWeRenotify
        return canWeRenotify
    }

    /**
     * handles messages from the clock stream.
     * checks that we are allowed to renotify.
     * checkes that the current time is within range of our notification time.
     * notifies or and updates state- or returns and does nothing.
     */
    private fun handleMessage(timestampMessage: Time) {
        // if we are disabled save processing and immediately return.
        if (this.disabled) {
            return
        }

        println("Handled: $timestampMessage for relationship ${this.name}")
        val date: Date = parseIso8601(timestampMessage)
        // only triggers when appropriate as per object configuration.
        val canBeTriggered = this.lastTriggered == null || canWeRenotify()
        if (!canBeTriggered) {
            return
        }

        if (this.trigger == null || this.trigger!!.time == null) {
            Timber.w("Relationship ${this.name} has no trigger or trigger time set - skipping")
            return
        }

        val currentTime: Long = date.time
        val triggerTime: Long = this.trigger!!.time!!.time + this.trigger!!.tempOffset!!
        val targetTime = triggerTime + this.offset

        // trigger to an accuracy of 5 mins.
        if (abs(currentTime - targetTime) < millisMins5) {
            // update the last trigger time upon successful triggering
            this.lastTriggered = getCurrentDate()
            println("Triggering a notification for ${this.name}, ${this.desc}")
            // handle notification
        }
    }

    /**
     * subscribes to the stream with a standard ID.
     */
    fun subscribeToClock() {
        subscriber.subscribe("${name}_subscription_id", ::handleMessage)
    }

    /**
     * Unsubscribes from stream with stored subscription ID.
     */
    fun unSubscribeToClock() {
        subscriber.unsubscribe("${name}_subscription_id")
    }
}
