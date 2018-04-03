package co.smartreceipts.android.model.impl

import android.os.Parcel
import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.currency.PriceCurrency
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.sync.model.SyncState
import co.smartreceipts.android.utils.TestLocaleToggler
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.sql.Date
import java.util.*

@RunWith(RobolectricTestRunner::class)
class ImmutableDistanceImplTest {

    companion object {

        private const val EPSILON = 1.0 / Distance.RATE_PRECISION

        private const val ID = 5
        private const val LOCATION = "Location"
        private val DISTANCE = BigDecimal(12.55)
        private val RATE = BigDecimal(0.33)
        private val DATE = Date(1409703721000L)
        private val CURRENCY = PriceCurrency.getInstance("USD")
        private val TIMEZONE = TimeZone.getDefault()
        private const val COMMENT = "Comment"
    }

    // Class under test
    private lateinit var distance: ImmutableDistanceImpl

    private lateinit var trip: Trip
    private lateinit var syncState: SyncState

    @Before
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
        trip = DefaultObjects.newDefaultTrip()
        syncState = DefaultObjects.newDefaultSyncState()
        distance = ImmutableDistanceImpl(
            ID,
            trip,
            LOCATION,
            DISTANCE,
            RATE,
            CURRENCY,
            DATE,
            TIMEZONE,
            COMMENT,
            syncState
        )
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
        val distance = ImmutableDistanceImpl(
            ID,
            trip,
            LOCATION,
            DISTANCE,
            BigDecimal(0.535),
            CURRENCY,
            DATE,
            TIMEZONE,
            COMMENT,
            syncState
        )
        assertEquals("$0.535", distance.currencyFormattedRate)
    }

    @Test
    fun getComment() {
        assertEquals(COMMENT, distance.comment)
    }

    @Test
    fun getSyncState() {
        Assert.assertEquals(syncState, distance.syncState)
    }

    @Test
    fun compareTo() {
        assertTrue(
            distance.compareTo(
                ImmutableDistanceImpl(
                    ID,
                    trip,
                    LOCATION,
                    DISTANCE,
                    RATE,
                    CURRENCY,
                    DATE,
                    TIMEZONE,
                    COMMENT,
                    syncState
                )
            ) == 0
        )
        assertTrue(
            distance.compareTo(
                ImmutableDistanceImpl(
                    ID,
                    trip,
                    LOCATION,
                    DISTANCE,
                    RATE,
                    CURRENCY,
                    Date(DATE.time * 2),
                    TIMEZONE,
                    COMMENT,
                    syncState
                )
            ) > 0
        )
        assertTrue(
            distance.compareTo(
                ImmutableDistanceImpl(
                    ID,
                    trip,
                    LOCATION,
                    DISTANCE,
                    RATE,
                    CURRENCY,
                    Date(0),
                    TIMEZONE,
                    COMMENT,
                    syncState
                )
            ) < 0
        )
    }

    @Test
    fun equals() {
        Assert.assertEquals(distance, distance)
        Assert.assertEquals(
            distance,
            ImmutableDistanceImpl(
                ID,
                trip,
                LOCATION,
                DISTANCE,
                RATE,
                CURRENCY,
                DATE,
                TIMEZONE,
                COMMENT,
                syncState
            )
        )
        assertThat(distance, not(equalTo(Any())))
        assertThat(distance, not(equalTo(mock(Distance::class.java))))
        assertThat(
            distance,
            not(
                equalTo(
                    ImmutableDistanceImpl(
                        -1,
                        trip,
                        LOCATION,
                        DISTANCE,
                        RATE,
                        CURRENCY,
                        DATE,
                        TIMEZONE,
                        COMMENT,
                        syncState
                    )
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    ImmutableDistanceImpl(
                        ID,
                        mock(Trip::class.java),
                        LOCATION,
                        DISTANCE,
                        RATE,
                        CURRENCY,
                        DATE,
                        TIMEZONE,
                        COMMENT,
                        syncState
                    )
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    ImmutableDistanceImpl(
                        ID,
                        trip,
                        "bad",
                        DISTANCE,
                        RATE,
                        CURRENCY,
                        DATE,
                        TIMEZONE,
                        COMMENT,
                        syncState
                    )
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    ImmutableDistanceImpl(
                        ID,
                        trip,
                        LOCATION,
                        BigDecimal(0),
                        RATE,
                        CURRENCY,
                        DATE,
                        TIMEZONE,
                        COMMENT,
                        syncState
                    )
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    ImmutableDistanceImpl(
                        ID,
                        trip,
                        LOCATION,
                        DISTANCE,
                        BigDecimal(0),
                        CURRENCY,
                        DATE,
                        TIMEZONE,
                        COMMENT,
                        syncState
                    )
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    ImmutableDistanceImpl(
                        ID,
                        trip,
                        LOCATION,
                        DISTANCE,
                        RATE,
                        PriceCurrency.getInstance("EUR"),
                        DATE,
                        TIMEZONE,
                        COMMENT,
                        syncState
                    )
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    ImmutableDistanceImpl(
                        ID,
                        trip,
                        LOCATION,
                        DISTANCE,
                        RATE,
                        CURRENCY,
                        Date(System.currentTimeMillis()),
                        TIMEZONE,
                        COMMENT,
                        syncState
                    )
                )
            )
        )
        assertThat(
            distance,
            not(
                equalTo(
                    ImmutableDistanceImpl(
                        ID,
                        trip,
                        LOCATION,
                        DISTANCE,
                        RATE,
                        CURRENCY,
                        DATE,
                        TIMEZONE,
                        "bad",
                        syncState
                    )
                )
            )
        )
    }

    @Test
    fun parcelEquality() {
        val parcel = Parcel.obtain()
        distance.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val distance = ImmutableDistanceImpl.CREATOR.createFromParcel(parcel)
        assertNotNull(distance)
        assertEquals(distance, this.distance)
    }
}