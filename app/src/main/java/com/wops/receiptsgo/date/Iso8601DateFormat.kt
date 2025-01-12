package com.wops.receiptsgo.date

import java.text.SimpleDateFormat
import java.util.*

/**
 * Extends the [SimpleDateFormat] to follow the iso 8601 date spec
 */
class Iso8601DateFormat : SimpleDateFormat(FORMAT_STRING, Locale.US) {

    init {
        timeZone = TimeZone.getTimeZone("utc")
    }

    companion object {
        const val FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }
}