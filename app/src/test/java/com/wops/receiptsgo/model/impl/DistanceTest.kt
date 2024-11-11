package com.wops.receiptsgo.model.impl

import com.wops.receiptsgo.DefaultObjects
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.model.factory.DistanceBuilderFactory
import com.wops.receiptsgo.utils.TestLocaleToggler
import com.wops.receiptsgo.utils.testParcel
import co.smartreceipts.core.sync.model.SyncState
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.joda.money.CurrencyUnit
import org.junit.After
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.sql.Date
import java.util.*

@RunWith(RobolectricTestRunner::class)
class DistanceTest {

    companion object {

        private const val EPSILON = 1.0 / Distance.RATE_PRECISION

        private const val ID = 5
        private val DIST_UUID = UUID.randomUUID()
        private const val LOCATION = "Location"
        private val DISTANCE = BigDecimal(12.55)
        private val RATE = BigDecimal(0.33)
        private val DATE = Date(1409703721000L)
        private val CURRENCY = CurrencyUnit.USD
        private val TIMEZONE = TimeZone.getDefault()
        private const val COMMENT = "Comment"
        private const val LOCATION_HIDDEN_AUTO_COMPLETE = false
        private const val COMMENT_HIDDEN_AUTO_COMPLETE = false
    }

    // Class under test
    private lateinit var distance: Distance

    private lateinit var trip: Trip
    private lateinit var syncState: SyncState

    @Before
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
        trip = DefaultObjects.newDefaultTrip()
        syncState = DefaultObjects.newDefaultSyncState()
        distance = DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(trip).setLocation(LOCATION).setDistance(DISTANCE).setRate(RATE)
            .setCurrency(CURRENCY).setDate(DATE).setTimezone(TIMEZONE).setComment(COMMENT).setSyncState(syncState)
                .setLocationHiddenFromAutoComplete(LOCATION_HIDDEN_AUTO_COMPLETE).setCommentHiddenFromAutoComplete(COMMENT_HIDDEN_AUTO_COMPLETE).build()
    }

    @After
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    @Test
    fun getId() {
        assertEquals(ID, distance.id)
    }

    @Test
    fun getUuid() {
        assertEquals(DIST_UUID, distance.uuid)
    }

    @Test
    fun getTrip() {
        assertEquals(trip, distance.trip)
    }

    @Test
    fun getLocation() {
        assertEquals(LOCATION, distance.location)
    }

    @Test
    fun getDistance() {
        assertEquals(DISTANCE.toDouble(), distance.distance.toDouble(), EPSILON)
    }

    @Test
    fun getDecimalFormattedDistance() {
        assertEquals("12.55", distance.decimalFormattedDistance)
    }

    @Test
    fun getDate() {
        assertEquals(DATE, distance.date)
    }

    @Test
    fun getTimeZone() {
        assertEquals(TIMEZONE, distance.timeZone)
    }

    @Test
    fun getRate() {
        assertEquals(RATE.toDouble(), distance.rate.toDouble(), EPSILON)
    }

    @Test
    fun getDecimalFormattedRate() {
        assertEquals("0.330", distance.decimalFormattedRate)
    }

    @Test
    fun getCurrencyFormattedRate() {
        assertEquals("$0.33", distance.currencyFormattedRate)
    }

    @Test
    fun getCurrencyFormattedRateFor3DigitPrecisionRate() {
        val newDistance = DistanceBuilderFactory(distance).setRate(BigDecimal(0.535)).build()
        assertEquals("$0.535", newDistance.currencyFormattedRate)
    }

    @Test
    fun getComment() {
        assertEquals(COMMENT, distance.comment)
    }

    @Test
    fun getSyncState() {
        assertEquals(syncState, distance.syncState)
    }

    @Test
    fun getLocationHiddenFromAutoComplete() {
        assertEquals(LOCATION_HIDDEN_AUTO_COMPLETE, distance.autoCompleteMetadata.isLocationHiddenFromAutoComplete)
    }

    @Test
    fun getCommentHiddenFromAutoComplete() {
        assertEquals(COMMENT_HIDDEN_AUTO_COMPLETE, distance.autoCompleteMetadata.isCommentHiddenFromAutoComplete)
    }

    @Test
    fun compareTo() {
        assertTrue(
            distance.compareTo(
                DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(trip).setLocation(LOCATION).setDistance(DISTANCE).setRate(RATE)
                    .setCurrency(CURRENCY).setDate(DATE).setTimezone(TIMEZONE).setComment(COMMENT).setSyncState(syncState)
                        .setLocationHiddenFromAutoComplete(LOCATION_HIDDEN_AUTO_COMPLETE).setCommentHiddenFromAutoComplete(COMMENT_HIDDEN_AUTO_COMPLETE).build()
            ) == 0
        )
        assertTrue(
            distance.compareTo(
                DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(trip).setLocation(LOCATION).setDistance(DISTANCE).setRate(RATE)
                    .setCurrency(CURRENCY).setDate(DATE.time * 2).setTimezone(TIMEZONE).setComment(COMMENT).setSyncState(syncState)
                        .setLocationHiddenFromAutoComplete(LOCATION_HIDDEN_AUTO_COMPLETE).setCommentHiddenFromAutoComplete(COMMENT_HIDDEN_AUTO_COMPLETE).build()
            ) > 0
        )
        assertTrue(
            distance.compareTo(
                DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(trip).setLocation(LOCATION).setDistance(DISTANCE).setRate(RATE)
                    .setCurrency(CURRENCY).setDate(Date(0)).setTimezone(TIMEZONE).setComment(COMMENT).setSyncState(syncState)
                        .setLocationHiddenFromAutoComplete(LOCATION_HIDDEN_AUTO_COMPLETE).setCommentHiddenFromAutoComplete(COMMENT_HIDDEN_AUTO_COMPLETE).build()
            ) < 0
        )
    }

    @Test
    fun equals() {
        assertEquals(distance, distance)
        assertEquals(
            distance,
            DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(trip).setLocation(LOCATION).setDistance(DISTANCE).setRate(RATE)
                .setCurrency(CURRENCY).setDate(DATE).setTimezone(TIMEZONE).setComment(COMMENT).setSyncState(syncState)
                    .setLocationHiddenFromAutoComplete(LOCATION_HIDDEN_AUTO_COMPLETE).setCommentHiddenFromAutoComplete(COMMENT_HIDDEN_AUTO_COMPLETE).build()
        )
        assertThat(distance, not(equalTo(Any())))
        assertThat(distance, not(equalTo(mock(Distance::class.java))))
        assertThat(
            distance,
            not(
                equalTo(
                    DistanceBuilderFactory(-1).setUuid(DIST_UUID).setTrip(trip).setLocation(LOCATION).setDistance(DISTANCE).setRate(RATE)
                        .setCurrency(CURRENCY).setDate(DATE).setTimezone(TIMEZONE).setComment(COMMENT).setSyncState(syncState).build()
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(mock(Trip::class.java)).setLocation(LOCATION).setDistance(DISTANCE).setRate(
                        RATE
                    )
                        .setCurrency(CURRENCY).setDate(DATE).setTimezone(TIMEZONE).setComment(COMMENT).setSyncState(syncState).build()
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(trip).setLocation("bad").setDistance(DISTANCE).setRate(RATE)
                        .setCurrency(CURRENCY).setDate(DATE).setTimezone(TIMEZONE).setComment(COMMENT).setSyncState(syncState).build()
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(trip).setLocation(LOCATION).setDistance(BigDecimal(0))
                        .setRate(RATE).setCurrency(CURRENCY).setDate(DATE).setTimezone(TIMEZONE).setComment(COMMENT).setSyncState(syncState)
                        .build()
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(trip).setLocation(LOCATION).setDistance(DISTANCE)
                        .setRate(BigDecimal(0)).setCurrency(CURRENCY).setDate(DATE).setTimezone(TIMEZONE).setComment(COMMENT)
                        .setSyncState(syncState).build()
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(trip).setLocation(LOCATION).setDistance(DISTANCE).setRate(RATE)
                        .setCurrency(CurrencyUnit.EUR).setDate(DATE).setTimezone(TIMEZONE).setComment(COMMENT)
                        .setSyncState(syncState).build()
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(trip).setLocation(LOCATION).setDistance(DISTANCE).setRate(RATE)
                        .setCurrency(CURRENCY).setDate(Date(System.currentTimeMillis())).setTimezone(TIMEZONE).setComment(COMMENT)
                        .setSyncState(syncState).build()
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(trip).setLocation(LOCATION).setDistance(DISTANCE).setRate(RATE)
                        .setCurrency(CURRENCY).setDate(DATE).setTimezone(TIMEZONE).setComment("bad").setSyncState(syncState).build()
                )
            )
        )

        assertThat(
            distance,
            not(
                equalTo(
                    DistanceBuilderFactory(ID).setUuid(UUID.randomUUID()).setTrip(trip).setLocation(LOCATION).setDistance(DISTANCE)
                        .setRate(RATE).setCurrency(CURRENCY).setDate(DATE).setTimezone(TIMEZONE).setComment(COMMENT).setSyncState(syncState)
                        .build()
                )
            )
        )
    }

    @Test
    fun parcelEquality() {
        val distanceFromParcel = distance.testParcel()

        assertNotSame(distance, distanceFromParcel)
        assertEquals(distance, distanceFromParcel)

    }
}