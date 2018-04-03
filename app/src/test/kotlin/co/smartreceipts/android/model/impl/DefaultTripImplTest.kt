package co.smartreceipts.android.model.impl

import android.os.Parcel
import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.currency.PriceCurrency
import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Source
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.sync.model.SyncState
import co.smartreceipts.android.utils.TestLocaleToggler
import junit.framework.Assert.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.sql.Date
import java.util.*
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class DefaultTripImplTest {

    companion object {

        private val NAME = "TripName"
        private val DIRECTORY = File(File(NAME).absolutePath)
        private val START_DATE = Date(1409703721000L)
        private val END_DATE = Date(1409783794000L)
        private val START_TIMEZONE = TimeZone.getTimeZone(TimeZone.getAvailableIDs()[0])
        private val END_TIMEZONE = TimeZone.getTimeZone(TimeZone.getAvailableIDs()[1])
        private val COMMENT = "Comment"
        private val COST_CENTER = "Cost Center"
        private val CURRENCY = PriceCurrency.getInstance("USD")
    }

    // Class under test
    private lateinit var trip: DefaultTripImpl

    private lateinit var syncState: SyncState

    @Mock
    private var price: Price? = null

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        TestLocaleToggler.setDefaultLocale(Locale.US)
        syncState = DefaultObjects.newDefaultSyncState()
        trip = DefaultTripImpl(
            DIRECTORY,
            START_DATE,
            START_TIMEZONE,
            END_DATE,
            END_TIMEZONE,
            CURRENCY,
            COMMENT,
            COST_CENTER,
            Source.Undefined,
            syncState
        )
    }

    @After
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    @Test
    fun getName() {
        assertEquals(NAME, trip.name)
    }

    @Test
    fun getDirectory() {
        assertEquals(DIRECTORY, trip.directory)
    }

    @Test
    fun getDirectoryPath() {
        assertEquals(DIRECTORY.absolutePath, trip.directoryPath)
    }

    @Test
    fun getStartDate() {
        assertEquals(START_DATE, trip.startDate)
    }

    @Test
    fun getStartTimeZone() {
        assertEquals(START_TIMEZONE, trip.startTimeZone)
    }

    @Test
    fun getEndDate() {
        assertEquals(END_DATE, trip.endDate)
    }

    @Test
    fun getEndTimeZone() {
        assertEquals(END_TIMEZONE, trip.endTimeZone)
    }

    @Test
    fun isDateInsideTripBounds() {
        assertTrue(trip.isDateInsideTripBounds(START_DATE))
        assertTrue(trip.isDateInsideTripBounds(END_DATE))
        assertTrue(trip.isDateInsideTripBounds(Date(START_DATE.time + 10)))
        assertTrue(trip.isDateInsideTripBounds(Date(END_DATE.time - 10)))

        assertFalse(trip.isDateInsideTripBounds(Date(START_DATE.time - TimeUnit.DAYS.toMillis(2))))
        assertFalse(trip.isDateInsideTripBounds(Date(END_DATE.time + TimeUnit.DAYS.toMillis(2))))
    }

    @Test
    fun getPrice() {
        assertNotNull(trip.price)
        trip.price = price!!
        assertEquals(price, trip.price)
    }

    @Test
    fun getDailySubTotal() {
        assertNotNull(trip.dailySubTotal)
        trip.dailySubTotal = price!!
        assertEquals(price, trip.dailySubTotal)
    }

    @Test
    fun getTripCurrency() {
        assertEquals(CURRENCY, trip.tripCurrency)
    }

    @Test
    fun getDefaultCurrencyCode() {
        assertEquals(CURRENCY.currencyCode, trip.defaultCurrencyCode)
    }

    @Test
    fun getComment() {
        assertEquals(COMMENT, trip.comment)
    }

    @Test
    fun getCostCenter() {
        assertEquals(COST_CENTER, trip.costCenter)
    }

    @Test
    fun getFilter() {
        assertNull(trip.filter)
    }

    @Test
    fun getSyncState() {
        assertEquals(syncState, trip.syncState)
    }

    @Test
    fun compareTo() {
        assertTrue(
            trip.compareTo(
                DefaultTripImpl(
                    DIRECTORY,
                    START_DATE,
                    START_TIMEZONE,
                    END_DATE,
                    END_TIMEZONE,
                    CURRENCY,
                    COMMENT,
                    COST_CENTER,
                    Source.Undefined,
                    syncState
                )
            ) == 0
        )
        assertTrue(
            trip.compareTo(
                DefaultTripImpl(
                    DIRECTORY,
                    START_DATE,
                    START_TIMEZONE,
                    Date(END_DATE.time * 2),
                    END_TIMEZONE,
                    CURRENCY,
                    COMMENT,
                    COST_CENTER,
                    Source.Undefined,
                    syncState
                )
            ) > 0
        )
        assertTrue(
            trip.compareTo(
                DefaultTripImpl(
                    DIRECTORY,
                    START_DATE,
                    START_TIMEZONE,
                    Date(0),
                    END_TIMEZONE,
                    CURRENCY,
                    COMMENT,
                    COST_CENTER,
                    Source.Undefined,
                    syncState
                )
            ) < 0
        )
    }

    @Test
    fun equals() {
        assertEquals(trip, trip)
        assertEquals(
            trip,
            DefaultTripImpl(
                DIRECTORY,
                START_DATE,
                START_TIMEZONE,
                END_DATE,
                END_TIMEZONE,
                CURRENCY,
                COMMENT,
                COST_CENTER,
                Source.Undefined,
                syncState
            )
        )
        assertThat(trip, not(equalTo(Any())))
        assertThat(trip, not(equalTo(mock(Trip::class.java))))
        assertThat(
            trip,
            not(
                equalTo(
                    DefaultTripImpl(
                        File(""),
                        START_DATE,
                        START_TIMEZONE,
                        END_DATE,
                        END_TIMEZONE,
                        CURRENCY,
                        COMMENT,
                        COST_CENTER,
                        Source.Undefined,
                        syncState
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    DefaultTripImpl(
                        DIRECTORY,
                        Date(System.currentTimeMillis()),
                        START_TIMEZONE,
                        END_DATE,
                        END_TIMEZONE,
                        CURRENCY,
                        COMMENT,
                        COST_CENTER,
                        Source.Undefined,
                        syncState
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    DefaultTripImpl(
                        DIRECTORY,
                        START_DATE,
                        TimeZone.getTimeZone(TimeZone.getAvailableIDs()[2]),
                        END_DATE,
                        END_TIMEZONE,
                        CURRENCY,
                        COMMENT,
                        COST_CENTER,
                        Source.Undefined,
                        syncState
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    DefaultTripImpl(
                        DIRECTORY,
                        START_DATE,
                        START_TIMEZONE,
                        Date(System.currentTimeMillis()),
                        END_TIMEZONE,
                        CURRENCY,
                        COMMENT,
                        COST_CENTER,
                        Source.Undefined,
                        syncState
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    DefaultTripImpl(
                        DIRECTORY,
                        START_DATE,
                        START_TIMEZONE,
                        END_DATE,
                        TimeZone.getTimeZone(TimeZone.getAvailableIDs()[2]),
                        CURRENCY,
                        COMMENT,
                        COST_CENTER,
                        Source.Undefined,
                        syncState
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    DefaultTripImpl(
                        DIRECTORY,
                        START_DATE,
                        START_TIMEZONE,
                        END_DATE,
                        END_TIMEZONE,
                        PriceCurrency.getInstance("EUR"),
                        COMMENT,
                        COST_CENTER,
                        Source.Undefined,
                        syncState
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    DefaultTripImpl(
                        DIRECTORY,
                        START_DATE,
                        START_TIMEZONE,
                        END_DATE,
                        END_TIMEZONE,
                        CURRENCY,
                        "bad",
                        COST_CENTER,
                        Source.Undefined,
                        syncState
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    DefaultTripImpl(
                        DIRECTORY,
                        START_DATE,
                        START_TIMEZONE,
                        END_DATE,
                        END_TIMEZONE,
                        CURRENCY,
                        COMMENT,
                        "bad",
                        Source.Undefined,
                        syncState
                    )
                )
            )
        )

        // Special equals cases (source, price, and daily subtotal don't cound):
        val tripWithPrice = DefaultTripImpl(
            DIRECTORY,
            START_DATE,
            START_TIMEZONE,
            END_DATE,
            END_TIMEZONE,
            CURRENCY,
            COMMENT,
            COST_CENTER,
            Source.Undefined,
            syncState
        )
        val tripWithDailySubTotal = DefaultTripImpl(
            DIRECTORY,
            START_DATE,
            START_TIMEZONE,
            END_DATE,
            END_TIMEZONE,
            CURRENCY,
            COMMENT,
            COST_CENTER,
            Source.Undefined,
            syncState
        )
        tripWithPrice.price = price!!
        tripWithDailySubTotal.dailySubTotal = price!!
        assertEquals(
            trip,
            DefaultTripImpl(
                DIRECTORY,
                START_DATE,
                START_TIMEZONE,
                END_DATE,
                END_TIMEZONE,
                CURRENCY,
                COMMENT,
                COST_CENTER,
                Source.Parcel,
                syncState
            )
        )
        assertEquals(trip, tripWithPrice)
        assertEquals(trip, tripWithDailySubTotal)
    }

    @Test
    fun parcelEquality() {
        val parcel = Parcel.obtain()
        trip.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val trip = DefaultTripImpl.CREATOR.createFromParcel(parcel)
        assertNotNull(trip)
        assertEquals(trip, this.trip)
    }

}