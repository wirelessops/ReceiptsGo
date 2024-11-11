package com.wops.receiptsgo.date

import org.junit.Assert.*
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Iso8601DateFormatTest {

    @Test
    fun parse() {
        val iso8601DateFormat = Iso8601DateFormat()
        assertEquals(MILLIS, iso8601DateFormat.parse(ISO_8601).time)
    }

    @Test
    fun format() {
        val iso8601DateFormat = Iso8601DateFormat()
        assertEquals(ISO_8601, iso8601DateFormat.format(MILLIS))
    }

    companion object {
        private const val MILLIS = 1534975988000L
        private const val ISO_8601 = "2018-08-22T22:13:08.000Z"
    }
}