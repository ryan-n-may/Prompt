package com.example.prompt

import java.util.Date

class Trigger(
    var name: String,
    var desc: String = "",
    var timeString: String,
    var tempOffset: Long? = 0,
) {
    var time: Date? = null

    init {
        this.time = parseIso8601(this.timeString)
    }
}
